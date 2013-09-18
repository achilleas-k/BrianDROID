package org.briansimulator.briandroid;

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

    public ArrayList<Spike> spikes;
    public int nspikes = 0;
    public int max_idx = 0;

    public SpikeMonitor() {
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

    public ArrayList<Float>[] getSpikeArray() {
        ArrayList<Float>[] spikeArray = new ArrayList[max_idx];
        for (int idx=0; idx<this.max_idx; idx++) {
            // this is stupid
            spikeArray[idx] = this.getSpikes(idx);
        }
        return spikeArray;
    }
}