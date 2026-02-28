package com.whispertflite;

import com.whispertflite.utils.WavUtil;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class WavUtilTest {

    @Test
    public void testHeaderSize() {
        assertEquals(44, WavUtil.getHeaderSize());
    }

    @Test
    public void testWavOutputSize() {
        byte[] pcmData = new byte[1000];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);
        assertEquals(44 + 1000, wav.length);
    }

    @Test
    public void testRiffHeader() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // Check "RIFF" marker
        assertEquals('R', (char) wav[0]);
        assertEquals('I', (char) wav[1]);
        assertEquals('F', (char) wav[2]);
        assertEquals('F', (char) wav[3]);
    }

    @Test
    public void testWaveMarker() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // Check "WAVE" marker at offset 8
        assertEquals('W', (char) wav[8]);
        assertEquals('A', (char) wav[9]);
        assertEquals('V', (char) wav[10]);
        assertEquals('E', (char) wav[11]);
    }

    @Test
    public void testFmtSubchunk() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // Check "fmt " marker at offset 12
        assertEquals('f', (char) wav[12]);
        assertEquals('m', (char) wav[13]);
        assertEquals('t', (char) wav[14]);
        assertEquals(' ', (char) wav[15]);
    }

    @Test
    public void testSampleRate() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // Sample rate at offset 24 (little-endian int)
        ByteBuffer bb = ByteBuffer.wrap(wav, 24, 4).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(16000, bb.getInt());
    }

    @Test
    public void testBitsPerSample() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // Bits per sample at offset 34 (little-endian short)
        ByteBuffer bb = ByteBuffer.wrap(wav, 34, 2).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(16, bb.getShort());
    }

    @Test
    public void testNumChannels() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // NumChannels at offset 22 (little-endian short)
        ByteBuffer bb = ByteBuffer.wrap(wav, 22, 2).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bb.getShort());
    }

    @Test
    public void testChunkSize() {
        byte[] pcmData = new byte[1000];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // ChunkSize at offset 4 = 36 + dataSize
        ByteBuffer bb = ByteBuffer.wrap(wav, 4, 4).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(36 + 1000, bb.getInt());
    }

    @Test
    public void testDataSubchunk() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // "data" marker at offset 36
        assertEquals('d', (char) wav[36]);
        assertEquals('a', (char) wav[37]);
        assertEquals('t', (char) wav[38]);
        assertEquals('a', (char) wav[39]);

        // Data size at offset 40
        ByteBuffer bb = ByteBuffer.wrap(wav, 40, 4).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(100, bb.getInt());
    }

    @Test
    public void testPcmDataPreserved() {
        byte[] pcmData = {1, 2, 3, 4, 5};
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // PCM data starts at offset 44
        for (int i = 0; i < pcmData.length; i++) {
            assertEquals(pcmData[i], wav[44 + i]);
        }
    }

    @Test
    public void testEmptyPcmData() {
        byte[] pcmData = new byte[0];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);
        assertEquals(44, wav.length);
    }

    @Test
    public void testCustomParameters() {
        byte[] pcmData = new byte[200];
        byte[] wav = WavUtil.pcmToWav(pcmData, 44100, 2, 16);

        // Check sample rate (44100)
        ByteBuffer bb = ByteBuffer.wrap(wav, 24, 4).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(44100, bb.getInt());

        // Check channels (2)
        bb = ByteBuffer.wrap(wav, 22, 2).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(2, bb.getShort());
    }

    @Test
    public void testByteRate() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // ByteRate at offset 28 = sampleRate * channels * bitsPerSample/8
        // = 16000 * 1 * 16/8 = 32000
        ByteBuffer bb = ByteBuffer.wrap(wav, 28, 4).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(32000, bb.getInt());
    }

    @Test
    public void testAudioFormatPCM() {
        byte[] pcmData = new byte[100];
        byte[] wav = WavUtil.pcmToWav16kMono(pcmData);

        // AudioFormat at offset 20 = 1 (PCM)
        ByteBuffer bb = ByteBuffer.wrap(wav, 20, 2).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bb.getShort());
    }
}
