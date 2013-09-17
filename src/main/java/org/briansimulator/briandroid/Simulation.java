package org.briansimulator.briandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.TextView;


/**
 * The base code generation template for BrianDROID simulations
 */
public class Simulation extends AsyncTask<Void, String, Void> {

    private final static String LOGID = "org.briansimulator.briandroid.Simulation";
    Context bdContext;
    TextView statusTextView;
    float _duration = {{duration}};
    float t;
    float dt = {{dt}}f;
    int simstate = 0;

    long runtimeDuration = -1;

    private String simulationStatus;
    private final String DESCRIPTION = "%%% SIMULATION DESCRIPTION %%%";

    // renderscript engine objects
    private RenderScript mRS;
    private ScriptC_renderscript mScript;

    public void setAppContext(Context bdctx) {
        this.bdContext = bdctx;
    }

    public float getTime() {
        return t;
    }

    public float getDuration() {
        return _duration;
    }

    public String getStatusText() {
        return simulationStatus;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getRuntimeDuration() {
        return ""+runtimeDuration;
    }

    public int getSimState() {
        return simstate;
    }

    public void setStatusTextView(TextView textView) {
        this.statusTextView = textView;
    }

    protected void setStatusText(String statusText) {
        statusTextView.setText(statusText);
    }

    protected void appendStatusText(String statusText) {
        statusTextView.append(statusText);
    }

    //*********** GLOBAL VARS **********//

    {{ arrays }}

    public void setup() {
        publishProgress("Setting up simulation ...");
        mRS = RenderScript.create(bdContext);
        mScript = new ScriptC_renderscript(mRS);

        _init_arrays();
        _bind_allocations();

        Log.d(LOGID, "Memory allocation and binding complete.");
        publishProgress("DONE!\n");
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void writeToFile(float[][] monitor, String filename) {
        Log.d(LOGID, "Writing to file "+filename);
        if (isExternalStorageWritable()) {
            final int N = monitor.length;
            StringBuilder dataSB = new StringBuilder();
            for (int idx=0; idx<N; idx++) {
                float[] mon_idx = monitor[idx];
                for (double mi : mon_idx) {
                    dataSB.append(mi+" ");
                }
                dataSB.append("\n");
            }
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath()+"/BrianDROIDout/"); //TODO: optional save path
                dir.mkdirs();
                File spikesFile = new File(dir, filename);
                FileOutputStream spikesStream = new FileOutputStream(spikesFile);
                spikesStream.write(dataSB.toString().getBytes()); // this might be inefficient
                spikesStream.close();
                Log.d(LOGID, "DONE!");
            } catch (Exception e) {
                Log.e(LOGID, "File write failed!");
                e.printStackTrace();
            }
        }
    }

    private void writeToFile(ArrayList<?>[] monitor, String filename) {
        if (isExternalStorageWritable()) {
            final int N = monitor.length;
            StringBuilder dataSB = new StringBuilder();
            for (int idx=0; idx<N; idx++) {
                ArrayList<Double> mon_idx = (ArrayList<Double>)monitor[idx];
                for (double mi : mon_idx) {
                    dataSB.append(mi+" ");
                }
                dataSB.append("\n");
            }
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath()+"/BrianDROIDout/"); //TODO: optional save path
                dir.mkdirs();
                File spikesFile = new File(dir, filename);
                FileOutputStream spikesStream = new FileOutputStream(spikesFile);
                spikesStream.write(dataSB.toString().getBytes()); // this might be inefficient
                spikesStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... text) {
        appendStatusText(text[0]);
    }

    @Override
    protected Void doInBackground(Void... _ign) {
        run();
        return null;
    }

    //*********** MAIN LOOP *************
    public void run() {
        publishProgress("Starting main run code ...\n");
        Log.d(LOGID, "Starting run code ...");
        simstate = 1;
        {{ idx_initialisations }}
        publishProgress("Starting state updater loop ...\n");
        long sim_start = System.currentTimeMillis();
        for (t=0; t<_duration; t+=dt) {
            mScript.set_t(t);
            {{ kernel_calls }}
            mRS.finish();
        }
        publishProgress("Simulation complete!\n");
        runtimeDuration = System.currentTimeMillis()-sim_start;
        Log.d(LOGID, "DONE!");
        publishProgress("Main loop run time: "+runtimeDuration+" ms\n");
        simstate = 2;

    }



}
