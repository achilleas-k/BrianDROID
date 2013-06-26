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
    private String simStateOutput;


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
    float sound;
    float frequency;

    // coincidence detectors state variables
    float[] v;

    float[][] delays;

    ArrayList<Float>[] connectionBuffer; // for propagating activity with delays
    int bufferLength;

    public LickliderPitch() {
        setState(0);
    }

    public String toString() {
        return "Licklider model";
    }

    public void setup() {
        dt = (float)0.02*ms;
        maxDelay = 22*ms;
        tau_ear = 1*ms;
        sigma_ear = (float)0.1;
        min_freq = 50;
        max_freq = 1000;
        N = 300;
        tau = 1*ms;
        sigma = (float)0.1;
        duration = 50*ms;
        numsteps = (int)(duration/dt);
        x = new float[2];
        v = new float[N];
        bufferLength = (int)(maxDelay/dt);
    }

    float[][] connect(int N) {
        // connection delays
        float[][] delays = new float[2][N];
        Arrays.fill(delays[0], 0);
        float start = (float)Math.log(max_freq);
        float end = (float)Math.log(min_freq);
        float step = (end-start)/N;
        float logfreq = start;
        for (int n=0; n<N; n++) {
            delays[1][n] = (float)(1.0/Math.exp(logfreq));
            logfreq += step;
            Log.d(LOGID, "DELAY: "+delays[1][n]);
        }
        return delays;
    }


    float xi() {
        return (float)(rng.nextGaussian());
    }

    void updateSound(float t, float dt, ArrayList<Float>[] xrec) {
        // updates the sound signal and the receptor states
        // checks for spikes
        frequency = 200+200*t;
        sound = 5*(float)(Math.pow(Math.sin(2*Math.PI*frequency*t), 3));
        int N = x.length;
        for (int n=0; n<N; n++) {
            x[n] += dt*((sound-x[n])/tau_ear+sigma_ear*(float)(Math.sqrt(2.0/tau_ear))*xi());
            xrec[n].add(x[n]);
            if (x[n] > 1) {
                x[n] = 0; // reset
                connectionBuffer[n].add(t); // put spike at end of buffer
            }
        }
    }

    void updateNetwork(ArrayList<Float>[] vrec) {
        for (int n=0; n<N; n++) {
            v[n] += dt*(-v[n]/tau+sigma*(float)(Math.sqrt(2/tau))*xi());
            vrec[n].add(v[n]);
        }
    }

    void propagate(float t) {
        // propagate appropriate buffer values to receiving network
        // this is rather simple since we have full connectivity so we can skip the destination check
        int delay;
        for (int n_net=0; n_net<N; n_net++) { // coincidence detectors
            for (int n_rec=0; n_rec<2; n_rec++) { // receptors
                delay = (int)(delays[n_rec][n_net]/dt);
                for (Float t_spike : connectionBuffer[n_rec]) {
                    if (t_spike+delay >= t) { // >= due to float comparison issues
                        v[n_net] += 0.5; // weight = 0.5
                    } else if (t_spike+delay < t) {
                        // buffers are sorted/sequential, so if the first check fails,
                        // there's no need to keep searching
                        break;
                    }
                }
            }
        }
        // clean buffers
        for (int n_rec=0; n_rec<2; n_rec++) {
            for (Float t_spike : connectionBuffer[n_rec]) {
                if (t_spike+maxDelay >= t) {
                    connectionBuffer[n_rec].remove(t_spike);
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

    void saveData(String path, String filename, ArrayList<Float>[] data) {
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
            File spikesFile = new File(dir, filename);
            FileOutputStream spikesStream = new FileOutputStream(spikesFile);
            spikesStream.write(datasb.toString().getBytes());
            spikesStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        setState(1);
        simStateOutput = "Setting up simulation ...\n";
        publishProgress(simStateOutput);
        Arrays.fill(x, 0);
        Arrays.fill(v, 0);
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
        ArrayList<Float>[] xrec = new ArrayList[2];
        ArrayList<Float>[] vrec = new ArrayList[N]; // array of arraylist of Float
        for (int n=0; n<2; n++) {
            xrec[n] = new ArrayList<Float>();
        }
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Float>();
            vrec[n] = new ArrayList<Float>();
        }

        float t;
        int progress;
        long start = System.currentTimeMillis();
        simStateOutput += "Running simulation ...";
        publishProgress(simStateOutput);
        for (int h=0; h<numsteps; h++) {
            //publishProgress(simStateOutput+" "+h+"/"+numsteps);
            t = h*dt;
            updateSound(t, dt, xrec); // loop of 2
            updateNetwork(vrec); // loop of N (400)
            propagate(t); // loop of N (400)
            nspikes = checkSpike(t, nspikes, spikesrec); // loop of N
        }
        long wallClockDura = System.currentTimeMillis()-start;
        simStateOutput += "\nSimulation done.\nTime taken: "+wallClockDura+" ms \n";
        publishProgress(simStateOutput);
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Writing file(s) ...\n";
        publishProgress(simStateOutput);
        // save file with data
        ArrayList<Float>[] delays_al = new ArrayList[2];
        for (int i=0; i<2; i++) {
            delays_al[i] = new ArrayList<Float>();
            for (int j=0; j<N; j++) {
                delays_al[i].add(delays[i][j]);
            }
        }
        if (isExternalStorageWritable()) {
            Log.d("Licklider", "Writing data.");
            saveData("/briandroid.tmp/", "briandroidLicklider.spikes", spikesrec);
            saveData("/briandroid.tmp/", "briandroidLicklider.v", vrec);
            saveData("/briandroid.tmp/", "briandroidLicklider.x", xrec);
            saveData("/briandroid.tmp/", "briandroidLicklider.delays", delays_al);
        }
        Log.d("Licklider", "DONE!!!");
        simStateOutput += "Done!\n";
        publishProgress(simStateOutput);
        setState(2);
        return;
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
