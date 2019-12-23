package com.example.iot_project2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;

public class Transmitter {
    int sample_num = Configuration.SampleNum;
    int fs = Configuration.SamplingRate;
    int f0 = Configuration.StartFreq;
    int f1 = Configuration.EndFreq;
    double T = Configuration.T;
    int chirp_num = 100;

    public byte[] doubles2bytes(double[] ds) {
        byte[] ans = new byte[2 * ds.length];
        int idx = 0;
        for (final double dval: ds) {
            final short val = (short)(dval * 32767);
            ans[idx++] = (byte)(val & 0x00ff);
            ans[idx++] = (byte)((val & 0xff00) >>> 8);
        }
        return ans;
    }

    public byte[] generate_signal() {
        double[] t = new double[sample_num];
        for (int i = 0; i < sample_num; i++) t[i] = i * ((double)1 / fs);
        double[] chirp = Chirp.chirp(t, f0, T, f1);

        double[] message = new double[sample_num * 2 * chirp_num];
        for (int i = 0; i < chirp_num; i++) {
            for (int j = 0; j < sample_num; j++) message[i * 2 * sample_num + j] = chirp[j];
            for (int j = sample_num; j < 2 * sample_num; j++) message[i * 2 * sample_num + j] = 0;
        }
        return doubles2bytes(message);
    }

    public void write_result(String file_name) {
        File file = new File(file_name);
        if (file.exists()) {
            file.delete();
        }
        try { file.createNewFile(); } catch (IOException e){
            throw new IllegalStateException("unable to create " + file.toString());
        }

        byte[] input = generate_signal();

        int channels = 1;
        //每分钟录到的数据的字节数
        long byteRate = 16 * fs * channels / 8;


        try {
            FileOutputStream os = new FileOutputStream(file);
            int audio_len = input.length;
            AudioRecordFunc.WriteWaveFileHeader(os, audio_len, audio_len + 36, fs, channels, byteRate);
            os.write(input);
            os.close();
        } catch (Throwable t){
            Log.e("MainActivity", "failed to write");
        }
    }


}
