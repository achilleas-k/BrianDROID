package org.briansimulator.briandroidtemplate;

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

    public SpikeMonitor() {
        spikes = new ArrayList<Spike>();
    }

    public SpikeMonitor(ArrayList<Spike> spikes) {
        this.spikes = spikes;
    }

    public void setSize(int size) {
        this.spikes.ensureCapacity(size);
    }

    public int getSize() {
        return this.spikes.size();
    }

    public void addSpike(Spike spike) {
        this.spikes.add(spike);
    }

    public void addSpike(int i, float t) {
        this.spikes.add(new Spike(i, t));
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
}