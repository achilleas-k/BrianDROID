package org.briansimulator.briandroid.Simulations;

import android.R;
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
    private Allocation mInAllocation;
    private Allocation mOutAllocation;
    private ScriptC_stupdate mScript;


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
        // TODO: accept some sort of configuration object for dynamically setting up the parameters and simulation
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

        mScript = new ScriptC_stupdate(mRS, getResources(), R.raw.stupdate);
    }

    static int getBinomial(int n, float p) {
        // very crude
        // could also use apache commons library if it will make our lives easier
        int x = 0;
        for(int i = 0; i < n; i++) {
            if(Math.random() < p)
                x++;
        }
        return x;
    }

    static int[] shuffle(int[] anArray) {
        // we could use Collections to automatically shuffle, but I'd rather
        // stick with primitives if I can (at least for now)
        int[] shuffled = anArray.clone();
        int len = shuffled.length;
        int r, tmp;
        for (int max=len-1; max>0; max--) {
            r = rng.nextInt(max);
            tmp = shuffled[max];
            shuffled[max] = shuffled[r];
            shuffled[r] = tmp;
        }
        return shuffled;
    }

    static int[] randSample(int[] population, int k) {
        int[] shuffled = shuffle(population);
        return Arrays.copyOfRange(shuffled, 0, k);
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
            int k = getBinomial(N, p);
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
        float[] v__tmp = dS[0]; float[] ge__tmp = dS[1]; float[] gi__tmp = dS[2];

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
            //progress = 100.0*h/numsteps;
            //publishProgress(simStateOutput+progress+" %");
            t = h*dt;
            // EULER UPDATE CODE, this is the update code generated by Brian for the equations:
            // dv/dt = (-v+ge*(Ee-v)+gi*(Ei-v))*(1./taum) : volt
            // dge/dt = -ge*(1./taue) : 1
            // dgi/dt = -gi*(1./taui) : 1
            ArrayList<Integer> spikes_t = new ArrayList<Integer>(); // spikes for time t
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
