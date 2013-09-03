package org.briansimulator.briandroidtemplate;

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


/**
 * The base code generation template for BrianDROID simulations
 */
public class CodegenTemplate { //extends AsyncTask<Void, String, Void> {

    private final static String LOGID = "org.briansimulator.briandroidtemplate.CodegenTemplate";
    Context bdContext;
    float _duration;
    float dt;
    float t;
    float progress;

    private String simulationStatus;
    private final String DESCRIPTION = "%%% SIMULATION DESCRIPTION %%%";

    // renderscript engine objects
    private RenderScript mRS;
    private ScriptC_stateupdate mScript;

    public void setAppContext(Context bdctx) {
        this.bdContext = bdctx;
    }

    public float getTime() {
        return t;
    }

    public float getDuration() {
        return _duration;
    }

    public float getProgress() {
        return progress;
    }


    public String getStatusText() {
        return simulationStatus;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    protected void setStatusText(String statusText) {
        simulationStatus = statusText;
    }

    protected void appendStatusText(String extraText) {
        simulationStatus += extraText;
    }


    //*********** %% GLOBAL VARS %% **********//
    /*
     * For each state variable:
     * type[] %%VARNAME%%;
     * Allocation %%VARNAME%%_rs;
     *
     */

    // JAVA ARRAY DEFINITIONS
double[] _array_mynrngroup_I;
double[] _array_mynrngroup_h;
double[] _array_mynrngroup_m;
double[] _array_mynrngroup_n;
double[] _array_mynrngroup_v;
boolean[] _array_mynrngroup_not_refractory;


    // ALLOCATION DEFINITIONS
Allocation _array_mynrngroup_I_alloc;
Allocation _array_mynrngroup_h_alloc;
Allocation _array_mynrngroup_m_alloc;
Allocation _array_mynrngroup_n_alloc;
Allocation _array_mynrngroup_v_alloc;
Allocation _array_mynrngroup_not_refractory_alloc;


    Allocation idx_allocation;
    Allocation out;

    public void setup() {
        /*
         * duration and dt.
         */
        _duration = 1;
        dt = 0.0001f;
        mRS = RenderScript.create(bdContext);
        mScript = new ScriptC_stateupdate(mRS);
        // SIMULATION PARAMS (N and dt)
        mScript.set_numNeurons(100);
        mScript.set_dt(dt);

        /*
         * For each state variable:
         * %%VARNAME%% = new type[N];
         * %%VARNAME%%_rs = Allocation.createSized(mRS, Element.%RSTYPE%(mRS), N);
         * mScript.bind_%%VARNAME%%(%%VARNAME%%_rs);
         *
         * where %RSTYPE% is the renderscript type that corresponds to ``type``
         *
         */

        // JAVA ARRAY INITIALISATIONS
_array_mynrngroup_I = new double[100];
_array_mynrngroup_h = new double[100];
_array_mynrngroup_m = new double[100];
_array_mynrngroup_n = new double[100];
_array_mynrngroup_v = new double[100];
_array_mynrngroup_not_refractory = new boolean[100];


        // ALLOCATION INITIALISATIONS
_array_mynrngroup_I_alloc = Allocation.createSized(mRS, Element.F64(mRS), 100);
_array_mynrngroup_h_alloc = Allocation.createSized(mRS, Element.F64(mRS), 100);
_array_mynrngroup_m_alloc = Allocation.createSized(mRS, Element.F64(mRS), 100);
_array_mynrngroup_n_alloc = Allocation.createSized(mRS, Element.F64(mRS), 100);
_array_mynrngroup_v_alloc = Allocation.createSized(mRS, Element.F64(mRS), 100);
_array_mynrngroup_not_refractory_alloc = Allocation.createSized(mRS, Element.BOOLEAN(mRS), 100);


        // MEMORY BINDINGS
mScript.bind_array_mynrngroup_I(_array_mynrngroup_I_alloc);
mScript.bind_array_mynrngroup_h(_array_mynrngroup_h_alloc);
mScript.bind_array_mynrngroup_m(_array_mynrngroup_m_alloc);
mScript.bind_array_mynrngroup_n(_array_mynrngroup_n_alloc);
mScript.bind_array_mynrngroup_v(_array_mynrngroup_v_alloc);
mScript.bind_array_mynrngroup_not_refractory(_array_mynrngroup_not_refractory_alloc);


        Log.d(LOGID, "Memory allocation and binding complete.");
    }


    //*********** MAIN LOOP *************
    public void run() {
        Log.d(LOGID, "Starting run code ...");
        int nsteps = (int)(_duration/dt);
        int[] idx_arr = new int[100];
        for (int idx=0; idx<100; idx++) {
            idx_arr[idx] = idx;
        }
        idx_allocation.copyFrom(idx_arr);
        float[] zeros = new float[100];
        Arrays.fill(zeros, 0f);
        /*
         * For each state variable:
         * %%VARNAME%%_rs.copyFrom(zeros);
         *
         *
         * This is for initialising all state vars to zero.
         * May be redundant or unnecessary.
         *
         */

        for (t=0; t<_duration; t+=dt) {
            mScript.forEach_update(idx_allocation, out);
            mRS.finish();
        }
        Log.d(LOGID, "DONE!");

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





}
