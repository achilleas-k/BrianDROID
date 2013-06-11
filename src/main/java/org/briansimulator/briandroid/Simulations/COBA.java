package org.briansimulator.briandroid.Simulations;

import android.widget.TextView;
import java.lang.System;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Created by achilleas on 10/06/13.
 *
 * Implementation of simplifiedcoba.py found in the original brian repository,
 * which is in turn a standalone version of the COBA simulation.
 * Original description follows:
 *
 *
 * This is an implementation of a benchmark described
 * in the following review paper:
 *
 * Simulation of networks of spiking neurons: A review of tools and strategies (2006).
 * Brette, Rudolph, Carnevale, Hines, Beeman, Bower, Diesmann, Goodman, Harris, Zirpe,
 * Natschläger, Pecevski, Ermentrout, Djurfeldt, Lansner, Rochel, Vibert, Alvarez, Muller,
 * Davison, El Boustani and Destexhe.
 * Journal of Computational Neuroscience
 *
 * Benchmark 1: random network of integrate-and-fire neurons with exponential synaptic conductances
 *
 * Clock-driven implementation with Euler integration
 * (no spike time interpolation)
 *
 * R. Brette - Dec 2007
 *
 * =============================================================================
 *
 * TODO: Display progress on screen (text, progressbar, whatever)
 */
public class COBA {
    // global random number generator
    static Random rng = new Random();

    // units
    double second = 1.0;
    double ms = 0.001;
    double mV = 0.001;

    // parameters
    int N = 4000;
    int Ne = (int)(N*0.8);
    int Ni = N-Ne;
    int Nplot = 4;
    double dt = 0.1*ms;
    double T = 1*second;
    int numsteps = (int)(T/dt);
    double p = 0.02;
    double Vr = 0*mV;
    double Vt = 10*mV;
    double we = 6.0/10.0; // excitatory synaptic weight (voltage)
    double wi = 67.0/10.0; // inhibitory synaptic weight
    double refrac = 5*ms;

    // State variable S=[v;ge;gi] and variable used in Euler step
    // dS=[v';ge';gi;] used in the main loop below
    double[][] S = new double[3][N];
    double[][] dS = new double[3][N];

    public COBA() {

    }


    static int getBinomial(int n, double p) {
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


    public void run(TextView statusText) {
        for (int i=0; i<3; i++) {
            // probably unnecessary
            Arrays.fill(S[i], 0.0);
            Arrays.fill(dS[i], 0.0);
        }
        String simStateOutput = "Setting up simulation ...\n";
        statusText.setText(simStateOutput);
        double[] v = S[0]; double[] ge = S[1]; double[] gi = S[2];
        double[] v__tmp = dS[0]; double[] ge__tmp = dS[1]; double[] gi__tmp = dS[2];

        // last spike times, stores the most recent time that a neuron has
        // spiked, which is used for refractory periods
        double[] LS = new double[N];
        Arrays.fill(LS, -2*refrac);

        // Initialisation of state variables
        simStateOutput += "Initialising state variables ...\n";
        statusText.setText(simStateOutput);
        for (int i=0; i<N; i++) {
            S[0][i] = (rng.nextGaussian()*5-5)*mV;
            S[1][i] = rng.nextGaussian()*1.5+4;
            S[2][i] = rng.nextGaussian()*12+20;
        }

        // Weight matrix
        // Generate random connectivity matrix (note: no weights)
        simStateOutput += "Generating random connectivity matrix ...\n";
        statusText.setText(simStateOutput);
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
        statusText.setText(simStateOutput);
        // TODO: record v for testing
        int nspikes = 0;
        ArrayList<Double>[] spikesrec = new ArrayList[N]; // array of arraylist of Double
        for (int n=0; n<N; n++) {
            spikesrec[n] = new ArrayList<Double>();
        }

        double t;
        long start = System.currentTimeMillis();
        for (int h=0; h<numsteps; h++) { // using integer loop variable to avoid f.p. arithmetic issues
            t = h*dt;
            // EULER UPDATE CODE, this is the update code generated by Brian for the equations:
            // dv/dt = (-v+ge*(Ee-v)+gi*(Ei-v))*(1./taum) : volt
            // dge/dt = -ge*(1./taue) : 1
            // dgi/dt = -gi*(1./taui) : 1
            ArrayList<Integer> spikes_t = new ArrayList<Integer>(); // spikes for time t
            for (int n=0; n<N; n++) {
                v__tmp[n] = (-v[n]+ge[n]*(0.06-v[n])+gi[n]*(-0.02-v[n]))*(1.0/0.02);
                ge__tmp[n] = -ge[n]*(1.0/0.005);
                gi__tmp[n] = -gi[n]*(1.0/0.01);
                S[0][n] += dt*dS[0][n]; // TODO: check this update
                // spike check
                if (v[n] > Vt) {
                    spikes_t.add(n); // neuron n has spiked
                    LS[n] = t; // store last spike time for n
                    // record spikes here
                    //spikesrec[n].add(t); // caused an outOfMemory exception. How fun!
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

            // refractory period, if the last spike of neuron i was within
            // refrac of the current time, fix the value to Vr
            // --> clamp all neurons whose LS > t-refrac
            for (int n=0; n<N; n++) {
                if (LS[n] > t-refrac) {
                    // TODO: move this to the state update loop
                    v[n] = Vr;
                }
            }

            nspikes += spikes_t.size();
        }

        long wallclockDura = System.currentTimeMillis()-start;

        simStateOutput += "Simulation finished.\nTime taken: "+wallclockDura+" ms\n";
        simStateOutput += "Total spikes fired: "+nspikes+"\n";
        simStateOutput += "Done!\n";
        statusText.setText(simStateOutput);
    }

}
