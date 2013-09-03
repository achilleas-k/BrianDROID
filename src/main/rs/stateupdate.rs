#pragma version(1)
#pragma rs java_package_name(org.briansimulator.briandroidtemplate)

float dt;

// RENDERSCRIPT ARRAY DEFINITIONS
double *_array_mynrngroup_I;
double *_array_mynrngroup_h;
double *_array_mynrngroup_m;
double *_array_mynrngroup_n;
double *_array_mynrngroup_v;
bool *_array_mynrngroup_not_refractory;


// CONSTANT DECLARATIONS
const float El = -0.065f;
const float EK = -0.09f;
const float Cm = 2e-10f;
const float ENa = 0.05f;
const float mV = 0.001f;
const float VT = -0.063f;
const float g_na = 2e-05f;
const float ms = 0.001f;
const float gl = 1e-08f;
const float g_kd = 6e-06f;


int32_t __attribute__((kernel)) update(int32_t idx) {
    const int _neuron_idx = idx;
    // STATE UPDATERS FOR mynrngroup_stateupdater_codeobject
# ITERATE_ALL { _idx }

_vectorisation_idx = _idx
I = _array_mynrngroup_I
h = _array_mynrngroup_h
m = _array_mynrngroup_m
n = _array_mynrngroup_n
v = _array_mynrngroup_v
not_refractory = _array_mynrngroup_not_refractory
not_refractory = 1 * ((not_refractory) + (logical_not(v > -40 * mV)))
_BA_h = -0.32913969447672 * (2980.95798704173 * exp(0.2 * VT / mV) + 1.0 * exp(0.2 * v / mV)) * exp(0.055556 * v / mV) * exp((0.055556 * VT - 0.055556 * v) / mV) / ((981.151601102854 * exp(0.2 * VT / mV) + 0.32913969447672 * exp(0.2 * v / mV)) * exp(0.055556 * VT / mV) + 4.0 * exp(0.255556 * v / mV))
_h = -(_BA_h) + (_BA_h + h) * exp(-(dt) * ((981.151601102854 * exp(0.2 * VT / mV) + 0.32913969447672 * exp(0.2 * v / mV)) * exp(0.055556 * VT / mV) + 4.0 * exp(0.255556 * v / mV)) * exp(-0.055556 * v / mV) / (ms * (2980.95798704173 * exp(0.2 * VT / mV) + 1.0 * exp(0.2 * v / mV))))
_BA_v = -(EK * g_kd * n ** 4.0 + ENa * g_na * h * m ** 3.0 + El * gl + I) / (g_kd * n ** 4.0 + g_na * h * m ** 3.0 + gl)
_v = -(_BA_v) + (_BA_v + v) * exp(-(dt) * (g_kd * n ** 4.0 + g_na * h * m ** 3.0 + gl) / Cm)
_BA_m = (-1.0 * exp(0.2 * VT / mV) + 0.000335462627902512 * exp(0.2 * v / mV)) * (0.32 * VT + 4.16 * mV - 0.32 * v) * exp(0.25 * v / mV) / ((-1.0 * exp(0.2 * VT / mV) + 0.000335462627902512 * exp(0.2 * v / mV)) * (-0.32 * VT - 4.16 * mV + 0.32 * v) * exp(0.25 * v / mV) + (25.7903399171931 * exp(0.25 * VT / mV) - 1.0 * exp(0.25 * v / mV)) * (0.28 * VT + 11.2 * mV - 0.28 * v) * exp(0.2 * VT / mV))
_m = -(_BA_m) + (_BA_m + m) * exp(dt * ((-1.0 * exp(0.2 * VT / mV) + 0.000335462627902512 * exp(0.2 * v / mV)) * (-0.32 * VT - 4.16 * mV + 0.32 * v) * exp(0.25 * v / mV) + (25.7903399171931 * exp(0.25 * VT / mV) - 1.0 * exp(0.25 * v / mV)) * (0.28 * VT + 11.2 * mV - 0.28 * v) * exp(0.2 * VT / mV)) / (mV * ms * (-1.0 * exp(0.2 * VT / mV) + 0.000335462627902512 * exp(0.2 * v / mV)) * (25.7903399171931 * exp(0.25 * VT / mV) - 1.0 * exp(0.25 * v / mV))))
_BA_n = (0.032 * VT + 0.48 * mV - 0.032 * v) * exp(0.225 * v / mV) / (mV * (-12.8951699585965 * exp(0.2 * VT / mV) + 0.642012708343871 * exp(0.2 * v / mV)) * exp(0.025 * VT / mV) + (-0.032 * VT - 0.48 * mV + 0.032 * v) * exp(0.225 * v / mV))
_n = -(_BA_n) + (_BA_n + n) * exp(dt * (mV * (-12.8951699585965 * exp(0.2 * VT / mV) + 0.642012708343871 * exp(0.2 * v / mV)) * exp(0.025 * VT / mV) + (-0.032 * VT - 0.48 * mV + 0.032 * v) * exp(0.225 * v / mV)) * exp(-0.025 * v / mV) / (mV * ms * (20.0855369231877 * exp(0.2 * VT / mV) - 1.0 * exp(0.2 * v / mV))))
h = _h
v = _v
m = _m
n = _n
_array_mynrngroup_h[:] = h
_array_mynrngroup_v[:] = v
_array_mynrngroup_m[:] = m
_array_mynrngroup_not_refractory[:] = not_refractory
_array_mynrngroup_n[:] = n


    return _neuron_idx;
}




