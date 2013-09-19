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

    ArrayList<SpikeMonitor> monitors = new ArrayList<SpikeMonitor>();
    {{ arrays }}

    public void setup() {
        publishProgress("Setting up simulation ... ");
        mRS = RenderScript.create(bdContext);
        mScript = new ScriptC_renderscript(mRS);

        _init_arrays();
        _bind_allocations();

        Log.d(LOGID, "Memory allocation and binding complete.");
        publishProgress("DONE!\n");
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
        publishProgress("Initialising ... \n");
        Log.d(LOGID, "Starting run code ...");
        simstate = 1;
        {{ idx_initialisations }}
        {{ monitor_listing }}
        publishProgress("Running simulation ... ");
        long sim_start = System.currentTimeMillis();
        for (t=0; t<_duration; t+=dt) {
            mScript.set_t(t);
            {{ kernel_calls }}
        }
        publishProgress("Simulation complete!\n");
        runtimeDuration = System.currentTimeMillis()-sim_start;
        Log.d(LOGID, "DONE!");
        // NOTE: Spike monitor name is not handled by template
        if (monitors.size() > 0) {
            for (SpikeMonitor mon : monitors) {
                publishProgress("Neuron group "+mon.groupName+" fired "+mon.nspikes+".\n");
                publishProgress("Saving recorded spikes ... ");
                mon.writeToFile(mon.groupName+"_spikemonitor.txt");
                publishProgress("DONE!\n");
            }
        }
        publishProgress("Main loop run time: "+runtimeDuration+" ms\n");
        simstate = 2;
    }



}
