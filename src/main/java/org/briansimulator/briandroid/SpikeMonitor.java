package org.briansimulator.briandroid;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by achilleas on 16/09/13.
 */

class Spike {

    public final int i;
    public final float t;

    public Spike(int neuron, float t) {
        this.i = neuron;
        this.t = t;
    }
}

public class SpikeMonitor {

    private final static String LOGID = "org.briansimulator.briandroid.Spikemonitor";
    public ArrayList<Spike> spikes;
    public int nspikes = 0;
    public int max_idx = 0;
    public String groupName = "";

    public SpikeMonitor() {
        this.spikes = new ArrayList<Spike>();
    }

    public SpikeMonitor(String name) {
        this.groupName = name;
        this.spikes = new ArrayList<Spike>();
    }

    public SpikeMonitor(int N) {
        this.spikes = new ArrayList<Spike>();
        this.max_idx = N;
    }

    public void setSize(int size) {
        this.spikes.ensureCapacity(size);
    }

    public int getSize() {
        return this.spikes.size();
    }

    public void addSpike(Spike spike) {
        this.nspikes++;
        if (spike.i > this.max_idx) {
            this.max_idx = spike.i;
        }
        this.spikes.add(spike);
    }

    public void addSpike(int i, float t) {
        this.nspikes++;
        if (i > this.max_idx) {
            this.max_idx = i;
        }
        this.spikes.add(new Spike(i, t));
    }

    public void addSpikes(int[] spikespace, float t) {
        int numspikes = spikespace[spikespace.length-1];
        this.nspikes += numspikes;
        int i;
        for (int idx=0; idx<numspikes; idx++) {
            i = spikespace[idx];
            if (i > this.max_idx) {
                this.max_idx = i;
            }
            this.spikes.add(new Spike(i, t));
        }
    }

    public ArrayList<Float> getSpikes(int i) {
        ArrayList<Float> i_spikes = new ArrayList<Float>();
        for (Spike spike : this.spikes) {
            if (spike.i == i) {
                i_spikes.add(spike.t);
            }
        }
        return i_spikes;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /*
     * Save the spikes recorded by this SpikeMonitor to a text file on the
     * device's external storage.
     * The data is formatted in two rows of equal length, the first representing
     * the spike times while the second represents the neuron indices.
     *
     * The default save directory is ``/sdcard/BrianDROIDout/``.
     * Optional paths will be available in a future release.
     *
     */
    public void writeToFile(String filename) {
        if (isExternalStorageWritable()) {
            StringBuilder nrnSB = new StringBuilder();
            StringBuilder timeSB = new StringBuilder();
            for (Spike spk : spikes) {
                nrnSB.append(spk.i+" ");
                timeSB.append(spk.t+" ");
            }
            nrnSB.append("\n");
            timeSB.append("\n");
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                //TODO: optional save path
                File dir = new File(sdCard.getAbsolutePath()+"/BrianDROIDout/");
                dir.mkdirs();
                File spikesFile = new File(dir, filename);
                FileOutputStream spikesStream = new FileOutputStream(spikesFile);
                spikesStream.write(nrnSB.toString().getBytes()); // this seems weird
                spikesStream.write(timeSB.toString().getBytes());
                spikesStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w(LOGID, "External storage is not writable. Aborting save.");
        }
    }

}