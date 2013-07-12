#pragma version(1)
#pragma rs java_package_name(org.briansimulator.briandroid.Simulations)



int numNeurons;
float dt;
typedef struct StateVars {
    float v;
    float ge;
    float gi;
} NrnState;

void init() {}
NrnState *neurons;

void root() {

    for (int n=0; n<numNeurons; n++) {
        float v = neurons[n].v;
        float ge = neurons[n].ge;
        float gi = neurons[n].gi;

        // update state variables
        v += dt*(-v+ge*(0.06f-v)+gi*(-0.02f-v))*(1.0f/0.02f);
        ge += dt*(-ge*(1.0f/0.005f));
        gi += dt*(-gi*(1.0f/0.01f));
    }

}

void setStateVars(NrnState *Sin) {
    neurons = Sin;
}

void setNumNeurons(int N) {
    numNeurons = N;
}

void setTimeStep(float timestep) {
    dt = timestep;
}

// use rsForEach