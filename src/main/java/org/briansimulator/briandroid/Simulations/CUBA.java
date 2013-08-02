package org.briansimulator.briandroid.Simulations;

import android.os.Environment;
import android.util.Log;

import org.briansimulator.briandroid.ItemDetailActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Achilleas Koutsou on 19/06/13.
 */
public class CUBA extends Simulation {
    public ItemDetailActivity simActivity;
    public final String ID = "CUBA";

    int N;
    int Ne;
    int Ni;
    float dt;
    float T;
    int numsteps;
    float Vr;
    float Vt;
    float p;
    float we;
    float wi;

    float[][] A;
    float[] _C;
    float[][] S;

    public CUBA(ItemDetailActivity sa) {
        simActivity = sa;
        setState(0);
    }

    public String toString() {
        return "CUBA";
    }

    @Override
    public void setup() {
        N = 4000;
        Ne = (int)(N*0.8);
        Ni = N-Ne;
        dt = (float)0.1*ms;
        T = 200*ms;
        numsteps = (int)(T/dt);
        Vr = -60*mV;
        Vt = -50*mV;
        p = (float)0.02;
        we = (float)1.62*mV;
        wi = -9*mV;
    }


    static int getBinomial(int n, float p) {
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

    static float[][] multiply(float[][] A, float[][] B) {
        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;
        if (aColumns != bRows) {
            //TODO: throw error
            return null;
        }
        float[][] result = new float[aRows][bColumns];
        for (int i=0; i<aRows; i++) {
            for (int j=0; j<bColumns; j++) {
                result[i][j] = 0;
                for (int k=0; k<aColumns; k++) {
                    result[i][j] += A[i][k]*B[k][j];
                }
            }
        }
        return result;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    protected void onProgressUpdate(String... progress) {
        progressText.setText(progress[0]);
    }


    @Override
    public void run() {
        setState(1);
        String simStateOutput = "Setting up simulation ...\n";
        publishProgress(simStateOutput);
        // matrices for state update, S(t+dt)=A*S+_C
        A = new float[][] {{(float)0.99501248, (float)0.00493794, (float)0.00496265},
                {(float)0, (float)0.98019867, (float)0},
                {(float)0, (float)0, (float)0.99004983}};
        _C = new float[] {(float)-2.44388520e-04, (float)-8.58745657e-21, (float)6.90431479e-20};

        // Initialise state matrix and assign uniform random membrane potentials
        simStateOutput += "Initialising state variables ...\n";
        publishProgress(simStateOutput);
        S = new float[3][N];
        for (int i=0; i<N; i++) {
            S[0][i] = rng.nextFloat()*(Vt-Vr)+Vr;
            S[1][i] = 0;
            S[2][i] = 0;
        }

        // Weight matrix
        // Generate random connectivity matrix (note: no weights)
        simStateOutput += "Generating random connectivity matrix ...\n";
        publishProgress(simStateOutput);
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
        publishProgress(simStateOutput);
        int nspikes = 0;
        ArrayList<Float>[] spikesrec = new ArrayList[N]; // array of arraylist of Float
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Float>();
        }

        float t;
        simStateOutput += "Running simulation ... ";
        publishProgress(simStateOutput);
        long start = System.currentTimeMillis();
        for (int h=0; h<numsteps; h++) { // using integer loop variable to avoid f.p. arithmetic issues
            t = h*dt;
            ArrayList<Integer> spikes_t = new ArrayList<Integer>(); // spikes for time t
            S = multiply(A, S);
            for (int n=0; n<N; n++) {
                S[0][n] += _C[0];
                S[1][n] += _C[1];
                S[2][n] += _C[2];
                // spike check
                if (S[0][n] > Vt) {
                    spikes_t.add(n); // neuron n has spiked
                    // record spikes here
                    spikesrec[n].add(t);
                }
            }

            // spike propagation
            for (int s : spikes_t) {
                int[] targets = W.get(s);
                if (s < Ne) {
                    for (int tar : targets) {
                        S[1][tar] += we;
                    }
                } else {
                    for (int tar : targets) {
                        S[2][tar] += wi;
                    }
                }
                S[0][s] = Vr;
            }

            nspikes += spikes_t.size();
        }

        long wallclockDura = System.currentTimeMillis()-start;
        Log.d("CUBA", "Simulation done!");
        simStateOutput += "\nSimulation finished.\nTime taken: "+wallclockDura+" ms\n";
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Writing file(s) ...\n";
        publishProgress(simStateOutput);
        // save file with data
        if (isExternalStorageWritable()) {
            Log.d("CUBA", "Building output.");
            StringBuilder spikesString = new StringBuilder();
            for (int n=0; n<N; n++) {
                for (float sp : spikesrec[n]) {
                    spikesString.append(sp+" ");
                }
                spikesString.append("\n");
            }
            Log.d("CUBA", "Writing data.");
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath()+"/briandroid.tmp/"); //TODO: optional save path
                dir.mkdirs();
                String spikesFilename = "briandroidCUBA.spikes";
                File spikesFile = new File(dir, spikesFilename);
                FileOutputStream spikesStream = new FileOutputStream(spikesFile);
                spikesStream.write(spikesString.toString().getBytes()); // this might be inefficient
                spikesStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Log.d("CUBA", "DONE!!!");
        simStateOutput += "Done!\n";
        publishProgress(simStateOutput);
        setState(2);
        return;
    }
}
