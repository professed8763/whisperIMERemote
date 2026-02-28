package com.whispertflite;

import static org.junit.Assert.*;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;

@RunWith(RobolectricTestRunner.class)
public class WhisperInputMethodServiceTest {

    @Test
    public void serviceCanBeCreated() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();
        assertNotNull("Service should be created", service);
    }

    @Test
    public void onCreateInputViewInflatesLayout() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        assertNotNull("Input view should not be null", inputView);
    }

    @Test
    public void inputViewContainsAllRequiredButtons() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        assertNotNull("Record button should exist", inputView.findViewById(R.id.btnRecord));
        assertNotNull("Keyboard button should exist", inputView.findViewById(R.id.btnKeyboard));
        assertNotNull("Translate button should exist", inputView.findViewById(R.id.btnTranslate));
        assertNotNull("Mode auto button should exist", inputView.findViewById(R.id.btnModeAuto));
        assertNotNull("Enter button should exist", inputView.findViewById(R.id.btnEnter));
        assertNotNull("Delete button should exist", inputView.findViewById(R.id.btnDel));
    }

    @Test
    public void inputViewContainsProgressBarAndStatus() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        ProgressBar progressBar = inputView.findViewById(R.id.processing_bar);
        assertNotNull("Progress bar should exist", progressBar);

        TextView tvStatus = inputView.findViewById(R.id.tv_status);
        assertNotNull("Status text view should exist", tvStatus);
    }

    @Test
    public void modeAutoDefaultsToOff() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();

        LinearLayout layoutButtons = inputView.findViewById(R.id.layout_buttons);
        assertEquals("Buttons layout should be visible when modeAuto is off",
                View.VISIBLE, layoutButtons.getVisibility());
    }

    @Test
    public void modeAutoOnHidesButtonLayout() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        // Set modeAuto preference to true before creating input view
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(service);
        sp.edit().putBoolean("imeModeAuto", true).commit();

        View inputView = service.onCreateInputView();

        LinearLayout layoutButtons = inputView.findViewById(R.id.layout_buttons);
        assertEquals("Buttons layout should be hidden when modeAuto is on",
                View.GONE, layoutButtons.getVisibility());
    }

    @Test
    public void serviceOnDestroyDoesNotCrash() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        // Should not throw
        service.onDestroy();
    }

    @Test
    public void statusTextViewStartsHidden() {
        ServiceController<WhisperInputMethodService> controller =
                Robolectric.buildService(WhisperInputMethodService.class);
        WhisperInputMethodService service = controller.create().get();

        View inputView = service.onCreateInputView();
        TextView tvStatus = inputView.findViewById(R.id.tv_status);

        // Status text starts as GONE per the layout XML (android:visibility="gone")
        assertEquals("Status text should start hidden", View.GONE, tvStatus.getVisibility());
    }
}
