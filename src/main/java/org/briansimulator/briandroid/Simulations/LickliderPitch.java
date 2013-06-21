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
 * Spike-based adaptation of Licklider's model of pitch processing
 * (autocorrelation with delay lines) with phase locking.
 *
 * Created by achilleas on 19/06/13.
 */
public class LickliderPitch extends Simulation {

    private static String LOGID = "org.briansimulator.briandroid.LICKLIDERPITCH";


    float dt;
    float maxDelay;
    float tau_ear;
    float sigma_ear;
    float min_freq;
    float max_freq;

    int N;
    float tau;
    float sigma;
    float duration;

    int numsteps;


    // ear and sound state variables
    float[] x;
    float[] sound;
    float[] frequency;

    // coincidence detectors state variables
    float[] v;

    public LickliderPitch() {
        setState(0);
    }

    public void setup() {
        dt = (float)0.2*ms;
        maxDelay = 20*ms;
        tau_ear = 1*ms;
        sigma_ear = (float)0.1;
        min_freq = 50;
        max_freq = 1000;
        N = 300;
        tau = 1;
        sigma = (float)0.1;
        duration = 500*ms;
        numsteps = (int)(duration/dt);

        x = new float[2];
        sound = new float[2];
        frequency = new float[2];

        v = new float[N];
    }

    public float xi() {
        return (float)(rng.nextGaussian());
    }

    public void updateEar(float t, float dt) {
        int N = x.length;
        for (int n=0; n<N; n++) {
            sound[n] = (float)(Math.pow(5*Math.sin(2*Math.PI*frequency[n]*t), 3));
            frequency[n] = 200+200*t;
            x[n] += dt*((sound[n]-x[n])/tau_ear+sigma_ear*(float)(Math.sqrt(2.0/tau_ear))*xi());
        }
    }

    public void updateNetwork() {
        for (int n=0; n<N; n++) {
            v[n] += -v[n]/tau+sigma*(float)(Math.sqrt(2/tau))*xi();
        }
    }

    public void run() {

    }

    public void record() {
        setState(1);
        int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioRecord micRec = new AudioRecord(MediaRecorder.AudioSource.MIC, // microphone
                44100, // sampling rate (default 44100 for max compatibility)
                AudioFormat.CHANNEL_IN_MONO, // max compatibility
                AudioFormat.ENCODING_PCM_16BIT, // 8 or 16 bit
                4096); //1048576);                       // buffer size in bytes
        micRec.startRecording();
        byte[] audioBytes = new byte[4096];
        int bytesRead;
        ArrayList<Integer> audioData = new ArrayList<Integer>();
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start < 5000) {
            bytesRead = micRec.read(audioBytes, 0, 4096);
            for (byte b : audioBytes) {
                audioData.add((int)b);
            }
            publishProgress("RAW DATA: " + Arrays.toString(audioBytes) +
                    "\nBytes: " + bytesRead +
                    "\nMin buffer size: " + minBufferSize);
            try {
                //Thread.sleep(100);
            } catch (Exception e) {
                Log.e(LOGID, "Exception thrown during sleep. ");
                e.printStackTrace();
            }
        }
        try{
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath()+"/briandroid.tmp/"); //TODO: optional save path
            dir.mkdirs();
            String audioFilename = "audio.raw";
            File audioFile = new File(dir, audioFilename);
            FileOutputStream streamWriter = new FileOutputStream(audioFile);
            streamWriter.write(audioData.toString().getBytes()); // I'm aware of how ridiculous this is
            streamWriter.close();
        } catch (Exception e) {
            Log.e(LOGID, "Exception thrown while writing file");
            e.printStackTrace();
        }
        publishProgress("ALL DONE!");
        setState(2);
    }
}
