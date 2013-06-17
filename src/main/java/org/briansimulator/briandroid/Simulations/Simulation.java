package org.briansimulator.briandroid.Simulations;

import android.os.AsyncTask;

import java.util.Random;

/**
 * Abstract Simulation class. All simulations should implement this class and
 * override the run() and setup() methods.
 * Created by achilleas on 14/06/13.
 *
 *
 */
public abstract class Simulation extends AsyncTask<Void, String, Void> {

    // global random number generator
    static Random rng = new Random();
    int STATE = 0; // 0: new; 1: running; 2: finished

    // units
    final float second = 1.0f;
    final float ms = 0.001f;
    final float mV = 0.001f;

    abstract void run();

    public abstract void setup();

    protected Void doInBackground(Void... _) {
        run();
        return null;
    }
}
