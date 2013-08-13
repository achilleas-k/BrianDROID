import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/** Created by achilleas on 10/06/13.
 *
 * Java implementation of the standalone COBA simulation.
 *
 * Original description follows.
 * --
 * This is an implementation of a benchmark described in the following review
 * paper:
 *
 * Simulation of networks of spiking neurons: A review of tools and strategies
 * (2006).  Brette, Rudolph, Carnevale, Hines, Beeman, Bower, Diesmann,
 * Goodman, Harris, Zirpe, Natschläger, Pecevski, Ermentrout, Djurfeldt,
 * Lansner, Rochel, Vibert, Alvarez, Muller, Davison, El Boustani and Destexhe.
 * Journal of Computational Neuroscience
 *
 * Benchmark 1: random network of integrate-and-fire neurons with exponential
 * synaptic conductances
 *
 * Clock-driven implementation with Euler integration (no spike time
 * interpolation)
 *
 * R. Brette - Dec 2007
 *
 * =============================================================================
 *
 */
public class COBA {

    float second = 1;
    float ms = 0.001f*second;
    float volt = 1;
    float mV = 0.001f*volt;
    static Random rng = new Random();

    private String simulationStatus;
    private final String DESCRIPTION = "\n"
       +"N = 4000\n"
       +"duration = 1 second\n"
       +"\n"
       +"Model equations:\n"
       +"     dv/dt = (-v+ge*(Ee-v)+gi*(Ei-v))*(1./taum) : volt\n"
       +"     dge/dt = -ge*(1./taue) : 1\n"
       +"     dgi/dt = -gi*(1./taui) : 1\n";

    private int STATE;

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

    private void setState(int state) {
        STATE = state;
    }

    public COBA() {
        setState(0);
    }

    public void setup() {
        N = 4000;
        Ne = (int)(N*0.8);
        Ni = N-Ne;
        Nplot = 4;
        dt = 0.1f*ms;
        T = 1*second;
        numsteps = (int)(T/dt);
        p = 0.02f;
        Vr = 0*mV;
        Vt = 10*mV;
        we = 6.0f/10.0f; // excitatory synaptic weight (voltage)
        wi = 67.0f/10.0f; // inhibitory synaptic weight
        refrac = 5*ms;

        // State variable S=[v;ge;gi] and variable used in Euler step
        // dS=[v';ge';gi;] used in the main loop below
        S = new float[3][N];
        dS = new float[3][N];

    }

    public int getState() {
        return STATE;
    }

    public String getStatusText() {
        return simulationStatus;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    static int getBinomial(int n, float p) {
        // very crude (and inefficient)
        // could also use apache commons library if it will make our lives easier
        // TODO: rewrite random calls
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

    protected void setStatusText(String statusText) {
        simulationStatus = statusText;
    }

    protected void appendStatusText(String extraText) {
        simulationStatus += extraText;
    }

    public void run() {
        setState(1);
        for (int i=0; i<3; i++) {
            // probably unnecessary
            Arrays.fill(S[i], 0.0f);
            Arrays.fill(dS[i], 0.0f);
        }
        String simStateOutput = "Setting up simulation ...\n";
        setStatusText(simStateOutput);
        float[] v = S[0]; float[] ge = S[1]; float[] gi = S[2];
        float[] v__tmp = dS[0]; float[] ge__tmp = dS[1]; float[] gi__tmp = dS[2];

        // last spike times, stores the most recent time that a neuron has
        // spiked, which is used for refractory periods
        float[] LS = new float[N];
        Arrays.fill(LS, -2*refrac);

        // Initialisation of state variables
        simStateOutput += "Initialising state variables ...\n";
        setStatusText(simStateOutput);
        for (int i=0; i<N; i++) {
            //S[0][i] = (rng.nextGaussian()*5-5)*mV;
            //S[1][i] = rng.nextGaussian()*1.5+4;
            //S[2][i] = rng.nextGaussian()*12+20;
            S[0][i] = (rng.nextFloat()*5-5)*mV;
            S[1][i] = rng.nextFloat()*1.5f+4;
            S[2][i] = rng.nextFloat()*12+20;
        }

        // Weight matrix
        // Generate random connectivity matrix (note: no weights)
        simStateOutput += "Generating random connectivity matrix ...\n";
        setStatusText(simStateOutput);
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

        simStateOutput += "Setting up monitors ...\n";
        setStatusText(simStateOutput);
        int nspikes = 0;
        ArrayList<Float>[] spikesrec = new ArrayList[N]; // array of arraylist of Float
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Float>();
        }

        float t;
        long start = System.currentTimeMillis();
        simStateOutput += "Running simulation ... ";
        setStatusText(simStateOutput);
        float progress;
        for (int h=0; h<numsteps; h++) { // using integer loop variable to avoid f.p. arithmetic issues
            //progress = 100.0*h/numsteps;
            //setStatusText(simStateOutput+progress+" %");
            //System.out.print(": "+h+"/"+numsteps+"\r");
            t = h*dt;
            // EULER UPDATE CODE, this is the update code generated by Brian for the equations:
            // dv/dt = (-v+ge*(Ee-v)+gi*(Ei-v))*(1./taum) : volt
            // dge/dt = -ge*(1./taue) : 1
            // dgi/dt = -gi*(1./taui) : 1
            ArrayList<Integer> spikes_t = new ArrayList<Integer>(); // spikes for time t
            for (int n=0; n<N; n++) {
                System.out.flush();
                v__tmp[n] = (-v[n]+ge[n]*(0.06f-v[n])+gi[n]*(-0.02f-v[n]))*(1.0f/0.02f);
                ge__tmp[n] = -ge[n]*(1.0f/0.005f);
                gi__tmp[n] = -gi[n]*(1.0f/0.01f);
                S[0][n] += dt*dS[0][n];
                S[1][n] += dt*dS[1][n];
                S[2][n] += dt*dS[2][n];
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
        simStateOutput += "\nSimulation finished.\nTime taken: "+wallclockDura+" ms\n";
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Writing file(s) ...\n";
        setStatusText(simStateOutput);
        // save file with data
        //if (isExternalStorageWritable()) {
        //    StringBuilder spikesString = new StringBuilder();
        //    for (int n=0; n<N; n++) {
        //        for (float sp : spikesrec[n]) {
        //            spikesString.append(sp+" ");
        //        }
        //        spikesString.append("\n");
        //    }
        //    //try {
        //    //    File sdCard = Environment.getExternalStorageDirectory();
        //    //    File dir = new File(sdCard.getAbsolutePath()+"/briandroid.tmp/"); //TODO: optional save path
        //    //    dir.mkdirs();
        //    //    String spikesFilename = "briandroidCOBA.spikes";
        //    //    File spikesFile = new File(dir, spikesFilename);
        //    //    FileOutputStream spikesStream = new FileOutputStream(spikesFile);
        //    //    spikesStream.write(spikesString.toString().getBytes()); // this might be inefficient
        //    //    spikesStream.close();
        //    //} catch (Exception e) {
        //    //    e.printStackTrace();
        //    //}

        //}
        simStateOutput += "Done!\n";
        setStatusText(simStateOutput);
        setState(2);
        return;
    }

    /*
     * main() is not required for BrianDROID, but is included here for
     * testing and debugging using the desktop JVM.
     */
    public static void main(String[] args) {
        COBA me = new COBA();
        me.setup();
        me.run();
    }

}
