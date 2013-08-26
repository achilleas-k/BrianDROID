package org.briansimulator.briandroidtemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.Context;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;


/**
 * Created by achilleas on 15/07/13.
 */
public class RenderscriptTest {

    private final static String LOGID = "org.briansimulator.briandroidtemplate.RenderscriptTest";
    Context bdContext;
    final static Random rng = new Random();
    final float _duration = 2.0f;
    final float dt = 0.0001f;
    float t;
    float progress;

    private String simulationStatus;
    private final String DESCRIPTION = "THE RENDERSCRIPT CLASS";

    // renderscript engine objects
    private RenderScript mRS;
    private ScriptC_stupdate mScript;

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


    //*********** GLOBAL VARS **********
    double[] _array_gp_I;
    double[] _array_gp_v;
    double[] _array_gp_h;
    double[] _array_gp_n;
    double[] _array_gp_m;
    boolean[] _array_gp_not_refractory;
    Allocation _array_gp_I_rs;
    Allocation _array_gp_v_rs;
    Allocation _array_gp_h_rs;
    Allocation _array_gp_n_rs;
    Allocation _array_gp_m_rs;
    Allocation _array_gp_not_refractory_rs;

    Allocation idx_allocation;
    Allocation out;

    private void createScript() {
        System.out.println("Starting createScrtipt ...");
        mRS = RenderScript.create(bdContext);
        System.out.println("mRS initialised ...");
        mScript = new ScriptC_stupdate(mRS);
        System.out.println("mScript initialised!");
        mScript.set_numNeurons(100);
        mScript.set_dt(dt);
        System.out.println("Script created ...");
        System.out.println("Allocating memory ...");

        _array_gp_I_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_v_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_h_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_n_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_m_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_not_refractory_rs = Allocation.createSized(mRS, Element.BOOLEAN(mRS), 100);

        mScript.bind_array_gp_I(_array_gp_I_rs);
        mScript.bind_array_gp_v(_array_gp_v_rs);
        mScript.bind_array_gp_h(_array_gp_h_rs);
        mScript.bind_array_gp_n(_array_gp_n_rs);
        mScript.bind_array_gp_m(_array_gp_m_rs);
        mScript.bind_array_gp_not_refractory(_array_gp_not_refractory_rs);



        // the following are probably not necessary
        idx_allocation = Allocation.createSized(mRS, Element.I32(mRS), 100);
        out = Allocation.createSized(mRS, Element.I32(mRS), 100);
        System.out.println("Memory allocation complete!");
    }

    //*********** SETUP METHOD *********
    public void setup() {
        _array_gp_I = new double [100];
        _array_gp_v = new double [100];
        _array_gp_h = new double [100];
        _array_gp_n = new double [100];
        _array_gp_m = new double [100];
        _array_gp_not_refractory = new boolean [100];

        createScript();
    }

    long fullParallelVarsDuration;
    long serialVarsDuration;
    public long getFullParallelVarsDuration() {
        return fullParallelVarsDuration;
    }
    public long getSerialVarsDuration() {
        return serialVarsDuration;
    }
    //*********** MAIN LOOP *************
    public void run() {
        int nsteps = (int)(_duration/dt);
        int t_int;
        float[][] vrec = new float[nsteps][100];
        int[] idx_arr = new int[100];
        for (int idx=0; idx<100; idx++) {
            idx_arr[idx] = idx;
        }
        idx_allocation.copyFrom(idx_arr);
        t_int = 0;
        float[] zeros = new float[100];
        Arrays.fill(zeros, 0f);
        _array_gp_I_rs.copyFrom(zeros);
        _array_gp_v_rs.copyFrom(zeros);
        _array_gp_h_rs.copyFrom(zeros);
        _array_gp_n_rs.copyFrom(zeros);
        _array_gp_m_rs.copyFrom(zeros);
        Log.d(LOGID, "Starting serial state updating");
        long start = System.currentTimeMillis();
        for (t=0; t<_duration; t+=dt) {
            mScript.forEach_updateall(idx_allocation, out);
            mRS.finish();
        }
        serialVarsDuration = System.currentTimeMillis()-start;
        Log.d(LOGID, "Finished serial state updating. Resetting ...");
        zeros = new float[100];
        Arrays.fill(zeros, 0f);
        _array_gp_I_rs.copyFrom(zeros);
        _array_gp_v_rs.copyFrom(zeros);
        _array_gp_h_rs.copyFrom(zeros);
        _array_gp_n_rs.copyFrom(zeros);
        _array_gp_m_rs.copyFrom(zeros);
        Log.d(LOGID, "Starting parallel state updating");
        start = System.currentTimeMillis();
        for (t=0; t<_duration; t+=dt) {
            mScript.forEach_hupdate(idx_allocation, out);
            mScript.forEach_mupdate(idx_allocation, out);
            mScript.forEach_vupdate(idx_allocation, out);
            mScript.forEach_nupdate(idx_allocation, out);
            mRS.finish();

        }
        fullParallelVarsDuration = System.currentTimeMillis()-start;
        Log.d(LOGID, "DONE!");
        //writeToFile(vrec, "renderscriptv");

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
