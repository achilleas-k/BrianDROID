package org.briansimulator.briandroid.Simulations;

import android.os.AsyncTask;
import android.widget.TextView;

import java.util.Random;

/**
 * Abstract Simulation class. All simulations should implement this class and
 * override the run() and setup() methods.
 *
 *
 * Created by Achilleas Koutsou on 14/06/13.
 *
 */
public abstract class Simulation extends AsyncTask<Void, String, Void> {

    // global random number generator
    static Random rng = new Random();
    int STATE = 0; // 0: new; 1: running; 2: finished
    protected TextView progressText;

    // units
    final float second = (float)1.0;
    final float ms = (float)0.001;
    final float mV = (float)0.001;

    abstract public void run();

    public abstract void setup();

    protected Void doInBackground(Void... _) {
        run();
        return null;
    }

    protected void setState(int s) {
        STATE = s;
    }

    public int getState() {
        return STATE;
    }

    public void setProgressView(TextView tv) {
        progressText = tv;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        progressText.setText(progress[0]);
    }
}
