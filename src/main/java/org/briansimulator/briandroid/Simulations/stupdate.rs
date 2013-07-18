#pragma version(1)
#pragma rs java_package_name(org.briansimulator.briandroid.Simulations)

int numNeurons;
float dt;
float *v;
float *ge;
float *gi;
rs_script gScript;
rs_allocation gIn;
rs_allocation gOut;

// should be called per row
void root(const int32_t *v_in, int32_t *v_out) {
    v_out = 0;
    for (int n=0; n<numNeurons; n++) {
        v[n] += dt*(-v[n]+ge[n]*(0.06f-v[n])+gi[n]*(-0.02f-v[n]))*(1.0f/0.02f);
        ge[n] += dt*(-ge[n]*(1.0f/0.005f));
        gi[n] += dt*(-gi[n]*(1.0f/0.01f));
    }
}


void filter() {
    rsForEach(gScript, gIn, gOut);
}


