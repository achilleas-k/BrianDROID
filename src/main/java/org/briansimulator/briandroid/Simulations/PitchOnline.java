package org.briansimulator.briandroid.Simulations;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Online version of PitchOffline.java
 * Reads data from the microphone and runs it through the Licklider pitch
 * perception model in real time.
 *
 *
 * Created by achilleas on 08/07/13.
 */
public class PitchOnline extends Simulation {
    public String ID = "PitchOnline";

    private static String LOGID = "org.briansimulator.briandroid.ONLINEPITCH";

    public PitchOnline() {
        setState(0);
    }

    public String toString() {
        return "Online pitch perception";
    }

    public void setup() {
    //    dt = (float)0.02*ms;
    //    maxDelay = 22*ms;
    //    tau_ear = 1*ms;
    //    sigma_ear = (float)0.1;
    //    min_freq = 50;
    //    max_freq = 1000;
    //    N = 300;
    //    tau = 1*ms;
    //    sigma = (float)0.1;
    //    x = new float[2];
    //    v = new float[N];
    //    xLS = new float[2];
    //    xrefr = 2*ms;
    }

    public void run() {
        setState(1);
        int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioRecord micRec = new AudioRecord(MediaRecorder.AudioSource.MIC, // microphone
                44100, // sampling rate (default 44100 for max compatibility)
                AudioFormat.CHANNEL_IN_MONO, // max compatibility
                AudioFormat.ENCODING_PCM_16BIT, // 8 or 16 bit
                40960);                       // buffer size in bytes
        micRec.startRecording();
        byte[] audioBytes = new byte[4096];
        int bytesRead = 0;
        ArrayList<Integer> audioData = new ArrayList<Integer>();
        publishProgress("Running ...");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start < 5000) {
            bytesRead += micRec.read(audioBytes, 0, 4096);
            publishProgress("Running ...\n"+bytesRead+" bytes read.");
            for (byte b : audioBytes) {
                audioData.add((int)b);
            }
            //publishProgress("RAW DATA: " + Arrays.toString(audioBytes) +
            //        "\nBytes: " + bytesRead +
            //        "\nMin buffer size: " + minBufferSize);
            try {
                //Thread.sleep(100);
            } catch (Exception e) {
                Log.e(LOGID, "Exception thrown during sleep. ");
                e.printStackTrace();
            }
        }
        micRec.release();
        publishProgress("Recording stopped.\nTotal bytes read: "+bytesRead+"\nSaving ...");
        try{
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath()+"/briandroid.tmp/"); //TODO: optional save path
            dir.mkdirs();
            String audioFilename = "audio.raw";
            File audioFile = new File(dir, audioFilename);
            FileOutputStream streamWriter = new FileOutputStream(audioFile);
            for (Integer ad : audioData)
                streamWriter.write(ad);
            streamWriter.close();
        } catch (Exception e) {
            Log.e(LOGID, "Exception thrown while writing file");
            e.printStackTrace();
        }
        publishProgress("ALL DONE!");
        setState(2);
    }
}
