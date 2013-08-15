package BDStandalone;

import java.util.Random;
import java.util.ArrayList;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;


/**
 * Created by achilleas on 15/07/13.
 */
public class RenderscriptTest {

    final static Random rng = new Random();
    final float _duration = 0.5f;
    final float dt = 0.0001f;
    float t;
    float progress;

    private String simulationStatus;
    private final String DESCRIPTION = "THE RENDERSCRIPT CLASS";

    // renderscript engine objects
    private RenderScript mRS;
    private ScriptC_stupdate mScript;


    public float getTime() {
        return t;
    }

    public float getDuration() {
        return _duration;
    }

    public float getProgress() {
        return progress;
    }


    public String getStatusText() {
        return simulationStatus;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    protected void setStatusText(String statusText) {
        simulationStatus = statusText;
    }

    protected void appendStatusText(String extraText) {
        simulationStatus += extraText;
    }

    //*********** GLOBAL VARS **********
    double[] _array_gp_I;
    double[] _array_gp_v;
    double[] _array_gp_h;
    double[] _array_gp_n;
    double[] _array_gp_m;
    boolean[] _array_gp_not_refractory;
    Allocation _array_gp_I_rs;
    Allocation _array_gp_v_rs;
    Allocation _array_gp_h_rs;
    Allocation _array_gp_n_rs;
    Allocation _array_gp_m_rs;
    Allocation _array_gp_not_refractory_rs;


    private void createScript() {
        //mRS = RenderScript.create(simActivity.getApplicationContext());

        //mScript = new ScriptC_stupdate(mRS, simActivity.getResources(),
        //        org.briansimulator.briandroid.R.raw.stupdate);
        mScript.set_numNeurons(100);
        mScript.set_dt(dt);

        _array_gp_I_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_v_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_h_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_n_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_m_rs = Allocation.createSized(mRS, Element.F32(mRS),  100);
        _array_gp_not_refractory_rs = Allocation.createSized(mRS, Element.BOOLEAN(mRS), 100);

        mScript.bind_array_gp_I(_array_gp_I_rs);
        mScript.bind_array_gp_v(_array_gp_v_rs);
        mScript.bind_array_gp_h(_array_gp_h_rs);
        mScript.bind_array_gp_n(_array_gp_n_rs);
        mScript.bind_array_gp_m(_array_gp_m_rs);
        mScript.bind_array_gp_not_refractory(_array_gp_not_refractory_rs);

    }

    //*********** SETUP METHOD *********
    public void setup() {
        _array_gp_I = new double [100];
        _array_gp_v = new double [100];
        _array_gp_h = new double [100];
        _array_gp_n = new double [100];
        _array_gp_m = new double [100];
        _array_gp_not_refractory = new boolean [100];


    }

    //*********** MAIN LOOP *************
    public void run() {
        final int _num_neurons = 100;
        final double ms = 0.001;
        final int _numm = 100;
        final double Cm = 2e-10;
        final double El = -0.065;
        final double g_na = 2e-05;
        final double VT = -0.063;
        final double mV = 0.001;
        final double ENa = 0.05;
        final double EK = -0.09;
        final int _numI = 100;
        final double g_kd = 6e-06;
        final double gl = 1e-08;
        final int _numh = 100;
        final int _numn = 100;
        final int _numv = 100;
        final int _numnot_refractory = 100;
        for (t=0; t<_duration; t+=dt) {
            for(int _neuron_idx=0; _neuron_idx<_num_neurons; _neuron_idx++) {
            }

        }
    }



}
