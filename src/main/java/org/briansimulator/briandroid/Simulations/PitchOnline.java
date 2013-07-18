package org.briansimulator.briandroid.Simulations;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import org.briansimulator.briandroid.SimulationActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Online version of PitchOffline.java
 * Reads data from the microphone and runs it through the Licklider pitch
 * perception model in real time.
 *
 *
 * Created by achilleas on 08/07/13.
 */
public class PitchOnline extends Simulation {
    public SimulationActivity simActivity;
    public String ID = "PitchOnline";

    private static String LOGID = "org.briansimulator.briandroid.ONLINEPITCH";

    private String simStateOutput;

    int samplingRate;
    int bufferSize;
    int soundChunkSize;
    float dt;
    float maxDelay;
    float tau_ear;
    float sigma_ear;
    float min_freq;
    float max_freq;

    int N;
    float tau;
    float sigma;

    // ear and sound state variables
    float[] x;
    float frequency;
    float[] xLS; // last spike
    float xrefr;

    // coincidence detectors state variables
    float[] v;

    float[][] delays;

    ArrayList<Float>[] connectionBuffer; // for propagating activity with delays

    short[] sound;

    public PitchOnline(SimulationActivity sa) {
        simActivity = sa;
        setState(0);
    }

    public String toString() {
        return "Online pitch perception";
    }

    public void setup() {
        samplingRate = 44100;
        bufferSize = 2*samplingRate;
        soundChunkSize = (int)(1000*ms*samplingRate);
        dt = (float)(1.0/samplingRate);
        maxDelay = 22*ms;
        tau_ear = 1*ms;
        sigma_ear = (float)0.1;
        min_freq = 50;
        max_freq = 1000;
        N = 300;
        tau = 1*ms;
        sigma = (float)0.1;
        x = new float[2];
        v = new float[N];
        xLS = new float[2];
        xrefr = 2*ms;
        sound = new short[soundChunkSize];
    }

    float[][] connect(int N) {
        // connection delays
        float[][] delays = new float[2][N];
        Arrays.fill(delays[0], 0);
        float start = (float)Math.log(min_freq);
        float end = (float)Math.log(max_freq);
        float step = (end-start)/N;
        float logfreq = start;
        for (int n=0; n<N; n++) {
            delays[1][n] = (float)(1.0/Math.exp(logfreq));
            logfreq += step;
        }
        return delays;
    }

    float xi() {

        return (float)(rng.nextGaussian()/Math.sqrt(dt));
    }

    void updateReceptors(float t, float dt, float sndSample) {
        // updates the receptor states
        // checks for spikes
        frequency = (float)(200.0+200.0*t);
        //sound = (float)(5.0*Math.pow(Math.sin(2.0*Math.PI*frequency*t), 3.0));
        int N = x.length;
        for (int n=0; n<N; n++) {
            if (xLS[n]+xrefr > t) {
                x[n] = 0;
            } else {
                x[n] += dt*((15.0*sndSample-x[n])/tau_ear+sigma_ear*(Math.sqrt(2.0/tau_ear))*xi());
            }
            //xrec[n].add(x[n]);
            if (x[n] > 1) {
                x[n] = 0; // reset
                xLS[n] = t;
                connectionBuffer[n].add(t); // put spike at end of buffer
            }
        }
    }

    void updateNetwork(float dt) {
        for (int n=0; n<N; n++) {
            v[n] += dt*(-v[n]/tau+sigma*(float)(Math.sqrt(2.0/tau))*xi());
        }
    }

    void propagate(float t, float dt) {
        // propagate appropriate buffer values to receiving network
        // this is rather simple since we have full connectivity so we can skip the destination check
        for (int n_net=0; n_net<N; n_net++) { // coincidence detectors
            for (int n_rec=0; n_rec<2; n_rec++) { // receptors
                float delay = delays[n_rec][n_net];
                for (Float t_spike : connectionBuffer[n_rec]) {
                    if ((t_spike+delay > t-dt) & (t_spike+delay < t+dt)) {
                        v[n_net] += 0.5; // weight = 0.5
                    }/* else if (h_spike+delay > h) {
                        // buffers are sorted/sequential, so there's no need to keep
                        // searching after t
                        break;
                    }*/
                }
            }
        }
        // clean buffers
        for (int n_rec=0; n_rec<2; n_rec++) {
            for (Iterator<Float> it = connectionBuffer[n_rec].iterator(); it.hasNext();) {
                Float t_spike = it.next();
                if (t_spike+maxDelay < t) {
                    it.remove();
                }
            }
        }
    }

    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    int checkSpike(float t, int nspikes, ArrayList<Float>[] spikesrec) {
        for (int n=0; n<N; n++) {
            if (v[n] > 1) { // threshold = 1
                spikesrec[n].add(t);
                nspikes++;
                v[n] = 0; // reset
            }
        }
        return nspikes;
    }

    long displaySimProgress(String simStateOutput, float t, float duration, long startTime, int nspikes, long lastReport) {
        // we should have progress updates on a separate thread
        long curTime = System.currentTimeMillis();
        if (curTime-lastReport > 1e4) {
            float progressPerc = t/duration;
            int secsElapsed = (int)((curTime-startTime)/1000);
            int estSecsRemaining = (int)((secsElapsed/progressPerc)-secsElapsed);
            String progressString = String.format("\n-- %.1f%% complete\n-- %ds elapsed, approximately %ds remaining ...\n-- %d spikes fired so far",
                    progressPerc*100, secsElapsed, estSecsRemaining, nspikes);
            publishProgress(simStateOutput+progressString);
            return curTime;
        }
        return lastReport;
    }

    void saveData(String path, String filename, ArrayList<Float> data) {
        /*
        Crashes when saving large amounts of data with OutOfMemoryError.
        Build and write one line at a time to avoid this.
         */
        StringBuilder datasb = new StringBuilder();
        for (float value : data) {
            datasb.append(value+" ");
        }
        datasb.append("\n");
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath()+path);
            dir.mkdirs();
            File savefile = new File(dir, filename);
            FileOutputStream savestream = new FileOutputStream(savefile);
            savestream.write(datasb.toString().getBytes());
            savestream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveData(String path, String filename, ArrayList<Float>[] data) {
        /*
        Crashes when saving large amounts of data with OutOfMemoryError.
        Build and write one line at a time to avoid this.
         */
        StringBuilder datasb = new StringBuilder();
        int dataLen = data.length;
        for (int n=0; n<dataLen; n++) {
            for (float value : data[n]) {
                datasb.append(value+" ");
            }
            datasb.append("\n");
        }
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath()+path);
            dir.mkdirs();
            File savefile = new File(dir, filename);
            FileOutputStream savestream = new FileOutputStream(savefile);
            savestream.write(datasb.toString().getBytes());
            savestream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    float squashSound(short sound) {
        return (float)sound/Short.MAX_VALUE;
    }

    public void run() {
        setState(1);
        simStateOutput = "Setting up simulation ...\n";
        publishProgress(simStateOutput);
        Arrays.fill(x, 0);
        Arrays.fill(v, 0);
        Arrays.fill(xLS, -xrefr);
        connectionBuffer = new ArrayList[2];
        connectionBuffer[0] = new ArrayList<Float>();
        connectionBuffer[1] = new ArrayList<Float>();

        simStateOutput += "Generating connection matrix ...\n";
        publishProgress(simStateOutput);
        delays = connect(N);

        simStateOutput += "Setting up monitors ...\n";
        publishProgress(simStateOutput);
        int nspikes = 0;
        ArrayList<Float>[] spikesrec = new ArrayList[N]; // array of arraylist of Float
        //ArrayList<Float>[] xrec = new ArrayList[2];
        //ArrayList<Float>[] vrec = new ArrayList[2]; // array of arraylist of Float
        //for (int n=0; n<2; n++) {
        //    xrec[n] = new ArrayList<Float>();
        //}
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Float>();
        }

        float t;
        long start = System.currentTimeMillis();
        simStateOutput += "Running simulation ...";
        publishProgress(simStateOutput);

        AudioRecord micRec = new AudioRecord(MediaRecorder.AudioSource.MIC, // microphone
                samplingRate,
                AudioFormat.CHANNEL_IN_MONO, // max compatibility
                AudioFormat.ENCODING_PCM_16BIT, // 8 or 16 bit
                bufferSize);
        ArrayList<Float> soundHistory = new ArrayList<Float>();
        micRec.startRecording();
        int samplesRead;
        int totSamplesRead = 0;
        int H = 0;
        while (totSamplesRead < soundChunkSize*4) {
            simStateOutput += "\nRecording ...\n";
            publishProgress(simStateOutput);
            samplesRead = micRec.read(sound, 0, soundChunkSize);
            totSamplesRead += samplesRead;
            if (samplesRead != soundChunkSize) {
                Log.w(LOGID, "WARNING: Sound buffer was not filled");
            }
            simStateOutput += "Read "+samplesRead+" samples, processing ...";
            publishProgress(simStateOutput);
            for (int h=0; h<samplesRead; h++, H++) {
                t = H*dt;
                float progress = (float)h/samplesRead*100;
                if (progress % 10 == 0)
                    publishProgress(simStateOutput+" "+progress+" %");
                float fltsnd = squashSound(sound[h]);
                updateReceptors(t, dt, fltsnd); // loop of 2
                updateNetwork(dt); // loop of N (400)
                propagate(t, dt); // loop of N (400)
                nspikes = checkSpike(t, nspikes, spikesrec); // loop of N
                soundHistory.add(fltsnd);
            }
            simStateOutput += "Done.\n";
            publishProgress(simStateOutput);
        }
        micRec.release();
        long wallClockDura = System.currentTimeMillis()-start;
        simStateOutput += "\nSimulation done.\nTime taken: "+wallClockDura+" ms \n";
        publishProgress(simStateOutput);
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Total samples processed: "+soundHistory.size()+"\n";
        simStateOutput += "Writing file(s) ...\n";
        publishProgress(simStateOutput);
        // save file with data
        if (isExternalStorageWritable()) {
            Log.d("Licklider", "Writing data.");
            saveData("/briandroid.tmp/", "pitchOnline.spikes", spikesrec);
            saveData("/briandroid.tmp/", "pitchOnline.sound", soundHistory);
        }
        Log.d("Licklider", "DONE!!!");
        simStateOutput += "Done!\n";
        publishProgress(simStateOutput);
        setState(2);
        return;
    }

}
