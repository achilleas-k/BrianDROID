package org.briansimulator.briandroid.Simulations;

import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by achilleas on 19/06/13.
 */
public class LickliderPitch extends Simulation {

    private static String LOGID = "org.briansimulator.briandroid.LICKLIDERPITCH";

    public LickliderPitch() {
        setState(0);
    }

    public void setup() {

    }

    public void run() {
        MediaRecorder micRec = new MediaRecorder();
        micRec.setAudioSource(MediaRecorder.AudioSource.MIC);
        micRec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        micRec.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        micRec.setOutputFile("/dev/null");
        try {
            micRec.prepare();
            micRec.start();
        } catch (Exception e) {
            Log.d(LOGID, "Failed to initialise microphone");
            Log.d(LOGID, "EXCEPTION THROWN: "+e.toString());
        }
        double amp;
        while (true) {
            amp = micRec.getMaxAmplitude();
            publishProgress("AMPLITUDE: "+amp);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.d(LOGID, "Exception thrown during sleep: "+e.toString());
            }
        }

    }
}
