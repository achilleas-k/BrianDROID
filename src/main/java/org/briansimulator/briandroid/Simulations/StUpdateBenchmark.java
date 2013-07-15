package org.briansimulator.briandroid.Simulations;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;

import org.briansimulator.briandroid.SimulationActivity;

/**
 * Created by achilleas on 15/07/13.
 */
public class StUpdateBenchmark extends Simulation {
    public SimulationActivity simActivity;
    public String ID = "Benchmark";

    private static String LOGID = "org.briansimulator.briandroid.BENCHMARK";


    private RenderScript mRS;
    private ScriptC_stupdate mScript;

    Allocation vPointer;
    Allocation gePointer;
    Allocation giPointer;

    Allocation vin;
    Allocation vout;

    private String simStateOutput;
    int N;
    int Ne;
    int Ni;
    float dt;
    float T;
    int numsteps;
    float p;
    float Vr;
    float Vt;
    float we; // excitatory synaptic weight (voltage)
    float wi; // inhibitory synaptic weight
    float refrac;

    // State variable S=[v;ge;gi] and variable used in Euler step
    // dS=[v';ge';gi;] used in the main loop below
    float[][] S;
    float[][] dS;


    public StUpdateBenchmark(SimulationActivity sa) {
        simActivity = sa;
        setState(0);
    }

    public String toString() {
        return "State Update Benchmark";
    }

    @Override
    public void setup() {
        // TODO: accept some sort of configuration object for dynamically setting up the parameters and simulation
        // parameters
        N = 4000;
        Ne = (int)(N*0.8);
        Ni = N-Ne;
        dt = 0.1f*ms;
        T = 1*second;
        numsteps = (int)(T/dt);
        p = (float)0.02;
        Vr = 0*mV;
        Vt = 10*mV;
        we = (float)6.0/(float)10.0; // excitatory synaptic weight (voltage)
        wi = (float)67.0/(float)10.0; // inhibitory synaptic weight
        refrac = 5*ms;

        // State variable S=[v;ge;gi] and variable used in Euler step
        // dS=[v';ge';gi;] used in the main loop below
        S = new float[3][N];
        dS = new float[3][N];

        createScript();
    }

    private void createScript() {
        mRS = RenderScript.create(simActivity.getApplicationContext());

        mScript = new ScriptC_stupdate(mRS, simActivity.getResources(),
                org.briansimulator.briandroid.R.raw.stupdate);
        mScript.set_numNeurons(N);
        mScript.set_dt(dt);

        vPointer = Allocation.createSized(mRS, Element.F32(mRS),  N);
        gePointer = Allocation.createSized(mRS, Element.F32(mRS),  N);
        giPointer = Allocation.createSized(mRS, Element.F32(mRS),  N);

        mScript.bind_v(vPointer);
        mScript.bind_ge(gePointer);
        mScript.bind_gi(giPointer);

        vin = Allocation.createSized(mRS, Element.I32(mRS), 1);
        vout = Allocation.createSized(mRS, Element.I32(mRS), 1);
    }

    public void run() {
        setState(1);
        simStateOutput = "Setting up simulation ...\n";
        publishProgress(simStateOutput);
        float[] v = S[0]; float[] ge = S[1]; float[] gi = S[2];
        float[] v__tmp = dS[0]; float[] ge__tmp = dS[1]; float[] gi__tmp = dS[2];
        long rsStart = System.currentTimeMillis();
        simStateOutput += "Running state updaters using renderscript ...";
        publishProgress(simStateOutput);
        int[] vin_arr = new int[2];
        float progress = 0;
        Log.d(LOGID, "Starting RS ================================================\n");
        for (int h=0; h<numsteps; h++) { // using integer loop variable to avoid f.p. arithmetic issues
            progress = 100f*h/numsteps;
            if (progress % 10 == 0) {
                Log.d(LOGID, "RS   "+progress+ " %");
            }
            vin_arr[0] = h;
            vin.copyFrom(vin_arr);
            vPointer.copyFrom(v);
            gePointer.copyFrom(ge);
            giPointer.copyFrom(gi);
            mScript.forEach_root(vin, vout);
            //mScript.invoke_filter();
        }
        mRS.finish();
        long rsTime = System.currentTimeMillis()-rsStart;
        simStateOutput += "\nRenderscript simulation done.\nTime taken: "+rsTime+" ms\n";
        long javaStart = System.currentTimeMillis();
        simStateOutput += "Running state updaters using Java code ...";
        publishProgress(simStateOutput);
        progress = 0;
        for (int h=0; h<numsteps; h++) {
            progress = 100f*h/numsteps;
            if (progress % 10 == 0) {
                Log.d(LOGID, "JAVA "+progress+ " %");
            }
            for (int n=0; n<N; n++) {
                v__tmp[n] = (-v[n]+ge[n]*(0.06f-v[n])+gi[n]*(-0.02f-v[n]))*(1.0f/0.02f);
                ge__tmp[n] = -ge[n]*(1.0f/0.005f);
                gi__tmp[n] = -gi[n]*(1.0f/0.01f);
                S[0][n] += dt*dS[0][n];
                S[1][n] += dt*dS[1][n];
                S[2][n] += dt*dS[2][n];
            }

        }
        long javaTime = System.currentTimeMillis()-javaStart;
        simStateOutput += "\nJava simulation done.\nTime taken: "+javaTime+" ms \n";
        publishProgress(simStateOutput);
        Log.d(LOGID, "RS time  : "+rsTime);
        Log.d(LOGID, "Java time: "+javaTime);
        setState(2);
        return;
    }


}
