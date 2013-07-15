package org.briansimulator.briandroid.Simulations;

import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;

import org.briansimulator.briandroid.SimulationActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Version of the COBA simulation that uses RenderScript for state updaters
 * Created by achilleas on 12/07/13.
 */
public class COBArs extends Simulation {
    public SimulationActivity simActivity;
    public final String ID = "COBArs";

    private RenderScript mRS;
    private ScriptC_stupdate mScript;

    Allocation vPointer;
    Allocation gePointer;
    Allocation giPointer;

    Allocation vin;
    Allocation vout;

    String simStateOutput;
    // parameters
    int N;
    int Ne;
    int Ni;
    int Nplot;
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

    public COBArs(SimulationActivity sa) {
        simActivity = sa;
        setState(0);
    }

    public String toString() {
        return "COBArs";
    }

    @Override
    public void setup() {
        // parameters
        N = 4000;
        Ne = (int)(N*0.8);
        Ni = N-Ne;
        Nplot = 4;
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

    static int[] randSample(int[] population, int k) {
        //int[] shuffled = shuffle(population);
        //return Arrays.copyOfRange(shuffled, 0, k);
        int[] firstk = new int[k];
        for (int i=0; i<k; i++) firstk[i] = population[i];
        return firstk;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    List<int[]> connect() {
        // Weight matrix
        // Generate random connectivity matrix (note: no weights)
        List<int[]> W = new ArrayList<int[]>();
        int[] population = new int[N];
        for (int i=0; i<N; i++) {
            population[i]=i;
        }
        for (int i=0; i<N; i++) {
            //int k = getBinomial(N, p);
            int k = (int)(N*p);
            int[] a = randSample(population, k);
            Arrays.sort(a);
            W.add(a);
        }
        return W;
    }

    @Override
    public void run() {
        setState(1);
        for (int i=0; i<3; i++) {
            // probably unnecessary
            Arrays.fill(S[i], 0.0f);
            Arrays.fill(dS[i], 0.0f);
        }
        simStateOutput = "Setting up simulation ...\n";
        publishProgress(simStateOutput);
        float[] v = S[0]; float[] ge = S[1]; float[] gi = S[2];

        // last spike times, stores the most recent time that a neuron has
        // spiked, which is used for refractory periods
        float[] LS = new float[N];
        Arrays.fill(LS, -2*refrac);

        // Initialisation of state variables
        simStateOutput += "Initialising state variables ...\n";
        publishProgress(simStateOutput);
        for (int i=0; i<N; i++) {
            S[0][i] = (float)(rng.nextGaussian()*5-5)*mV;
            S[1][i] = (float)(rng.nextGaussian()*1.5+4);
            S[2][i] = (float)(rng.nextGaussian()*12+20);
        }

        simStateOutput += "Generating random connectivity matrix ...\n";
        publishProgress(simStateOutput);
        List<int[]> W = connect();

        simStateOutput += "Setting up monitors ...\n";
        publishProgress(simStateOutput);
        int nspikes = 0;
        ArrayList<Float>[] spikesrec = new ArrayList[N]; // array of arraylist of Float
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Float>();
        }

        float t;
        long start = System.currentTimeMillis();
        simStateOutput += "Running simulation ... ";
        publishProgress(simStateOutput);
        float progress;
        for (int h=0; h<numsteps; h++) { // using integer loop variable to avoid f.p. arithmetic issues
            t = h*dt;
            vPointer.copyFrom(v);
            gePointer.copyFrom(ge);
            giPointer.copyFrom(gi);
            ArrayList<Integer> spikes_t = new ArrayList<Integer>(); // spikes for time t
            mScript.forEach_root(vin, vout);
            for (int n=0; n<N; n++) {
                // spike check
                if (v[n] > Vt) {
                    spikes_t.add(n); // neuron n has spiked
                    LS[n] = t; // store last spike time for n
                    // record spikes here
                    spikesrec[n].add(t);
                }
                // we can also check if we're in a refractory period before exiting the loop
                if (LS[n] > t-refrac) {
                    v[n] = Vr;
                }

            }

            // spike propagation
            for (int s : spikes_t) {
                int[] targets = W.get(s);
                if (s < Ne) {
                    for (int tar : targets) {
                        ge[tar] += we;
                    }
                } else {
                    for (int tar : targets) {
                        gi[tar] += wi;
                    }
                }
            }

            nspikes += spikes_t.size();
        }

        long wallclockDura = System.currentTimeMillis()-start;
        Log.d("COBA", "Simulation done!");
        simStateOutput += "\nSimulation finished.\nTime taken: "+wallclockDura+" ms\n";
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Writing file(s) ...\n";
        publishProgress(simStateOutput);
        // save file with data
        if (isExternalStorageWritable()) {
            Log.d("COBA", "Building output.");
            StringBuilder spikesString = new StringBuilder();
            for (int n=0; n<N; n++) {
                for (float sp : spikesrec[n]) {
                    spikesString.append(sp+" ");
                }
                spikesString.append("\n");
            }
            Log.d("COBA", "Writing data.");
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath()+"/briandroid.tmp/"); //TODO: optional save path
                dir.mkdirs();
                String spikesFilename = "briandroidCOBA.spikes";
                File spikesFile = new File(dir, spikesFilename);
                FileOutputStream spikesStream = new FileOutputStream(spikesFile);
                spikesStream.write(spikesString.toString().getBytes()); // this might be inefficient
                spikesStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Log.d("COBA", "DONE!!!");
        simStateOutput += "Done!\n";
        publishProgress(simStateOutput);
        setState(2);
        return;
    }
}
