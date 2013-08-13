import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/** Created by achilleas on 2013-08-09
 *
 */
public class SimpleCodeGen {

    // global vars for all simulations
    final static Random rng = new Random();
    float t;
    float _t_dt;
    final float _duration = 0.5f;
    final float dt = 0.0001f;
    final int _duration_dt = (int)(_duration/dt);

    private String simulationStatus;
    private final String DESCRIPTION = "**DESCRIPTION**";

    public float getTime() {
        return t;
    }

    public float getDuration() {
        return _duration;
    }

    public float getDt() {
        return dt;
    }

    public String getStatusText() {
        return simulationStatus;
    }

    public String getDescription() {
        return DESCRIPTION;
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



    // PASTE GENERATED CODE HERE

    //*********** PUBLIC VARS **********
    boolean[] _array_gp_not_refractory;
    double[] _array_gp_lastspike;
    double[] _array_gp_V;
    //*********** SETUP METHOD *********
    public void setup() {
        _array_gp_not_refractory = new boolean [1000];
        _array_gp_lastspike = new double [1000];
        _array_gp_V = new double [1000];
    }
    //*********** MAIN LOOP *************
    public void run() {
        final int _num_neurons = 1000;
        final int _numV = 1000;
        final int _numnot_refractory = 1000;
        final double tau = 0.01;
        final int _numlastspike = 1000;
        // USE_SPECIFIERS { _num_neurons }

        ////// SUPPORT CODE ///
        //

        ////// HANDLE DENORMALS ///
        //
        ////// HASH DEFINES ///////
        //
        ///// POINTERS ////////////
        //
        //// MAIN CODE ////////////
        for (_t_dt=0; _t_dt<_duration_dt; _t_dt+=1) {
            t = _t_dt*dt;
            for(int _neuron_idx=0; _neuron_idx<_num_neurons; _neuron_idx++)
            {
                final int _vectorisation_idx = _neuron_idx;
                final double lastspike = _array_gp_lastspike[_neuron_idx];
                boolean not_refractory = _array_gp_not_refractory[_neuron_idx];
                double V = _array_gp_V[_neuron_idx];
                not_refractory = t - lastspike > 0.005;
                final double _V = V * Math.exp(-(dt) * (not_refractory?1:0) / tau);
                V = _V;
                _array_gp_not_refractory[_neuron_idx] = not_refractory;
                _array_gp_V[_neuron_idx] = V;
            }
            System.out.print("\r"+t+"/"+_duration+"           ");
            System.out.flush();
        }
        System.out.println();


    }

}



