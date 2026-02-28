package com.whispertflite.asr;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.whispertflite.engine.WhisperEngine;
import com.whispertflite.engine.WhisperEngineJava;
import com.whispertflite.engine.WhisperEngineRemote;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Whisper {

    public interface WhisperListener {
        void onUpdateReceived(String message);
        void onResultReceived(WhisperResult result);
    }

    private static final String TAG = "Whisper";
    public static final String MSG_PROCESSING = "Processing...";
    public static final String MSG_PROCESSING_DONE = "Processing done...!";

    public static final Action ACTION_TRANSCRIBE = Action.TRANSCRIBE;
    public static final Action ACTION_TRANSLATE = Action.TRANSLATE;
    private String currentModelPath = "";

    public enum Action {
        TRANSLATE, TRANSCRIBE
    }

    private final AtomicBoolean mInProgress = new AtomicBoolean(false);

    private final WhisperEngine mWhisperEngine;
    private Action mAction;
    private int mLangToken = -1;
    private WhisperListener mUpdateListener;

    private final Lock taskLock = new ReentrantLock();
    private final Condition hasTask = taskLock.newCondition();
    private volatile boolean taskAvailable = false;

    /**
     * Creates a Whisper instance using the local TFLite engine.
     */
    public Whisper(Context context) {
        this(context, false);
    }

    /**
     * Creates a Whisper instance using either local or remote engine.
     *
     * @param context    Android context
     * @param useRemote  true to use remote API, false for local TFLite
     */
    public Whisper(Context context, boolean useRemote) {
        if (useRemote) {
            this.mWhisperEngine = new WhisperEngineRemote(context);
        } else {
            this.mWhisperEngine = new WhisperEngineJava(context);
        }

        // Start thread for RecordBuffer transcription
        Thread threadProcessRecordBuffer = new Thread(this::processRecordBufferLoop);
        threadProcessRecordBuffer.start();
    }

    /**
     * Checks if remote mode is enabled and properly configured.
     * Uses SharedPreferences to determine if the user has enabled remote API mode
     * AND has provided an API key.
     */
    public static boolean isRemoteMode(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useRemote = sp.getBoolean("useRemoteApi", false);
        String apiKey = sp.getString("apiKey", "");
        return useRemote && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Initializes the remote engine (no model files needed).
     * Call this instead of loadModel() when in remote mode.
     */
    public void initRemote() {
        try {
            mWhisperEngine.initialize("", "", true);
            currentModelPath = "remote";
        } catch (IOException e) {
            Log.e(TAG, "Error initializing remote engine", e);
            sendUpdate("Remote API initialization failed: " + e.getMessage());
        }
    }

    public void setListener(WhisperListener listener) {
        this.mUpdateListener = listener;
    }

    public void loadModel(File modelPath, File vocabPath, boolean isMultilingual) {
        loadModel(modelPath.getAbsolutePath(), vocabPath.getAbsolutePath(), isMultilingual);
        currentModelPath = modelPath.getAbsolutePath();
    }

    public void loadModel(String modelPath, String vocabPath, boolean isMultilingual) {
        try {
            mWhisperEngine.initialize(modelPath, vocabPath, isMultilingual);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing model...", e);
            sendUpdate("Model initialization failed");
        }
    }

    public String getCurrentModelPath(){
        return currentModelPath;
    }

    public void unloadModel() {
        mWhisperEngine.deinitialize();
        currentModelPath = "";
    }

    public void setAction(Action action) {
        this.mAction = action;
    }

    public void setLanguage(int language){
        this.mLangToken = language;
    }

    public void start() {
        if (!mInProgress.compareAndSet(false, true)) {
            Log.d(TAG, "Execution is already in progress...");
            return;
        }
        taskLock.lock();
        try {
            taskAvailable = true;
            hasTask.signal();
        } finally {
            taskLock.unlock();
        }
    }

    public void stop() {
        mInProgress.set(false);
    }

    public boolean isInProgress() {
        return mInProgress.get();
    }

    private void processRecordBufferLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            taskLock.lock();
            try {
                while (!taskAvailable) {
                    hasTask.await();
                }
                processRecordBuffer();
                taskAvailable = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                taskLock.unlock();
            }
        }
    }

    private void processRecordBuffer() {
        try {
            if (mWhisperEngine.isInitialized() && RecordBuffer.getOutputBuffer() != null) {
                long startTime = System.currentTimeMillis();
                sendUpdate(MSG_PROCESSING);

                WhisperResult whisperResult = null;
                synchronized (mWhisperEngine) {
                    whisperResult = mWhisperEngine.processRecordBuffer(mAction, mLangToken);
                }
                sendResult(whisperResult);

                long timeTaken = System.currentTimeMillis() - startTime;
                Log.d(TAG, "Time Taken for transcription: " + timeTaken + "ms");
                sendUpdate(MSG_PROCESSING_DONE);
            } else {
                sendUpdate("Engine not initialized or file path not set");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during transcription", e);
            sendUpdate("Transcription failed: " + e.getMessage());
        } finally {
            mInProgress.set(false);
        }
    }

    private void sendUpdate(String message) {
        if (mUpdateListener != null) {
            mUpdateListener.onUpdateReceived(message);
        }
    }

    private void sendResult(WhisperResult whisperResult) {
        if (mUpdateListener != null) {
            mUpdateListener.onResultReceived(whisperResult);
        }
    }

}
