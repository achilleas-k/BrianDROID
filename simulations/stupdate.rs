#pragma version(1)
#pragma rs java_package_name(BDStandalone)

int numNeurons;
float dt;
float *array_gp_I;
float *array_gp_v;
float *array_gp_h;
float *array_gp_n;
float *array_gp_m;
bool *array_gp_not_refractory;
rs_script gScript;
rs_allocation gIn;
rs_allocation gOut;
const int _numm = 100;
const float ms = 0.001f;
const float Cm = 2e-10f;
const float El = -0.065f;
const float g_na = 2e-05f;
const float VT = -0.063f;
const float mV = 0.001f;
const float ENa = 0.05f;
const float EK = -0.09f;
const float g_kd = 6e-06f;
const float gl = 1e-08f;
// should be called per row
void root(const int32_t *v_in, int32_t *v_out) {
    v_out = 0;

    for (int _neuron_idx=0; _neuron_idx<numNeurons; _neuron_idx++) {
                const int _vectorisation_idx = _neuron_idx;
                const float I = array_gp_I[_neuron_idx];
                float h = array_gp_h[_neuron_idx];
                float m = array_gp_m[_neuron_idx];
                float n = array_gp_n[_neuron_idx];
                float v = array_gp_v[_neuron_idx];
                bool not_refractory = array_gp_not_refractory[_neuron_idx];

                not_refractory = false;//((not_refractory) || (!(v > -40 * mV)));
                const float _BA_h = -0.32913969447672f * (2980.95798704173f * exp(0.2f * VT / mV) + 1.0f * exp(0.2f * v / mV)) * exp(0.055556f * v / mV) * exp((0.055556f * VT - 0.055556f * v) / mV) / ((981.151601102854f * exp(0.2f * VT / mV) + 0.32913969447672f * exp(0.2f * v / mV)) * exp(0.055556f * VT / mV) + 4.0f * exp(0.255556f * v / mV));
                const float _h = -(_BA_h) + (_BA_h + h) * exp(-(dt) * ((981.151601102854f * exp(0.2f * VT / mV) + 0.32913969447672f * exp(0.2f * v / mV)) * exp(0.055556f * VT / mV) + 4.0f * exp(0.255556f * v / mV)) * exp(-0.055556f * v / mV) / (ms * (2980.95798704173f * exp(0.2f * VT / mV) + 1.0f * exp(0.2f * v / mV))));
                const float _BA_v = -(EK * g_kd * pow(n, 4.0f) + ENa * g_na * h * pow(m, 3.0f) + El * gl + I) / (g_kd * pow(n, 4.0f) + g_na * h * pow(m, 3.0f) + gl);
                const float _v = -(_BA_v) + (_BA_v + v) * exp(-(dt) * (g_kd * pow(n, 4.0f) + g_na * h * pow(m, 3.0f) + gl) / Cm);
                const float _BA_m = (-1.0f * exp(0.2f * VT / mV) + 0.000335462627902512f * exp(0.2f * v / mV)) * (0.32f * VT + 4.16f * mV - 0.32f * v) * exp(0.25f * v / mV) / ((-1.0f * exp(0.2f * VT / mV) + 0.000335462627902512f * exp(0.2f * v / mV)) * (-0.32f * VT - 4.16f * mV + 0.32f * v) * exp(0.25f * v / mV) + (25.7903399171931f * exp(0.25f * VT / mV) - 1.0f * exp(0.25f * v / mV)) * (0.28f * VT + 11.2f * mV - 0.28f * v) * exp(0.2f * VT / mV));
                const float _m = -(_BA_m) + (_BA_m + m) * exp(dt * ((-1.0f * exp(0.2f * VT / mV) + 0.000335462627902512f * exp(0.2f * v / mV)) * (-0.32f * VT - 4.16f * mV + 0.32f * v) * exp(0.25f * v / mV) + (25.7903399171931f * exp(0.25f * VT / mV) - 1.0f * exp(0.25f * v / mV)) * (0.28f * VT + 11.2f * mV - 0.28f * v) * exp(0.2f * VT / mV)) / (mV * ms * (-1.0f * exp(0.2f * VT / mV) + 0.000335462627902512f * exp(0.2f * v / mV)) * (25.7903399171931f * exp(0.25f * VT / mV) - 1.0f * exp(0.25f * v / mV))));
                const float _BA_n = (0.032f * VT + 0.48f * mV - 0.032f * v) * exp(0.225f * v / mV) / (mV * (-12.8951699585965f * exp(0.2f * VT / mV) + 0.642012708343871f * exp(0.2f * v / mV)) * exp(0.025f * VT / mV) + (-0.032f * VT - 0.48f * mV + 0.032f * v) * exp(0.225f * v / mV));
                const float _n = -(_BA_n) + (_BA_n + n) * exp(dt * (mV * (-12.8951699585965f * exp(0.2f * VT / mV) + 0.642012708343871f * exp(0.2f * v / mV)) * exp(0.025f * VT / mV) + (-0.032f * VT - 0.48f * mV + 0.032f * v) * exp(0.225f * v / mV)) * exp(-0.025f * v / mV) / (mV * ms * (20.0855369231877f * exp(0.2f * VT / mV) - 1.0f * exp(0.2f * v / mV))));
                h = _h;
                v = _v;
                m = _m;
                n = _n;
                array_gp_h[_neuron_idx] = h;
                array_gp_v[_neuron_idx] = v;
                array_gp_m[_neuron_idx] = m;
                array_gp_not_refractory[_neuron_idx] = not_refractory;
                array_gp_n[_neuron_idx] = n;

    }
}




