package com.whispertflite.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility to convert raw PCM16 audio bytes into a WAV file byte array.
 * Pure Java, no Android dependencies — fully unit-testable.
 */
public class WavUtil {

    private static final int HEADER_SIZE = 44;
    private static final short AUDIO_FORMAT_PCM = 1;

    /**
     * Wraps raw PCM16 audio data in a WAV container.
     *
     * @param pcmData      Raw PCM16 audio bytes (little-endian, signed 16-bit)
     * @param sampleRate   Sample rate in Hz (e.g. 16000)
     * @param channels     Number of channels (1 = mono, 2 = stereo)
     * @param bitsPerSample Bits per sample (16)
     * @return Complete WAV file as byte array
     */
    public static byte[] pcmToWav(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) {
        int dataSize = pcmData.length;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        short blockAlign = (short) (channels * bitsPerSample / 8);

        ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        header.put((byte) 'R');
        header.put((byte) 'I');
        header.put((byte) 'F');
        header.put((byte) 'F');
        header.putInt(36 + dataSize); // ChunkSize = 36 + SubChunk2Size
        header.put((byte) 'W');
        header.put((byte) 'A');
        header.put((byte) 'V');
        header.put((byte) 'E');

        // fmt sub-chunk
        header.put((byte) 'f');
        header.put((byte) 'm');
        header.put((byte) 't');
        header.put((byte) ' ');
        header.putInt(16);                      // SubChunk1Size (16 for PCM)
        header.putShort(AUDIO_FORMAT_PCM);      // AudioFormat (1 = PCM)
        header.putShort((short) channels);      // NumChannels
        header.putInt(sampleRate);              // SampleRate
        header.putInt(byteRate);                // ByteRate
        header.putShort(blockAlign);            // BlockAlign
        header.putShort((short) bitsPerSample); // BitsPerSample

        // data sub-chunk
        header.put((byte) 'd');
        header.put((byte) 'a');
        header.put((byte) 't');
        header.put((byte) 'a');
        header.putInt(dataSize);                // SubChunk2Size

        // Combine header + PCM data
        byte[] wav = new byte[HEADER_SIZE + dataSize];
        System.arraycopy(header.array(), 0, wav, 0, HEADER_SIZE);
        System.arraycopy(pcmData, 0, wav, HEADER_SIZE, dataSize);

        return wav;
    }

    /**
     * Convenience method for Whisper's default format: 16kHz, mono, 16-bit.
     */
    public static byte[] pcmToWav16kMono(byte[] pcmData) {
        return pcmToWav(pcmData, 16000, 1, 16);
    }

    /**
     * Returns just the WAV header size (44 bytes).
     */
    public static int getHeaderSize() {
        return HEADER_SIZE;
    }
}
