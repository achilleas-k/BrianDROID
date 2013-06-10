package org.briansimulator.briandroid.Simulations;

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


    public void run() {
        // Init to 0 unnecessary, since we init to rand later
        //for (int i=0; i<3; i++) {
        //    Arrays.fill(S[i], 0.0);
        //    Arrays.fill(dS[i], 0.0);
        //}
        double[] v = S[0]; double[] ge = S[1]; double[] gi = S[2];
        double[] v__tmp = dS[0]; double[] ge__tmp = dS[1]; double[] gi__tmp = dS[2];

        // last spike times, stores the most recent time that a neuron has
        // spiked, which is used for refractory periods
        double[] LS = new double[N];
        Arrays.fill(LS, -2*refrac);

        // Initialisation of state variables
        for (int i=0; i<N; i++) {
            S[0][i] = (rng.nextGaussian()*5-5)*mV;
            S[1][i] = rng.nextGaussian()*1.5+4;
            S[2][i] = rng.nextGaussian()*12+20;
        }

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

    }

}
