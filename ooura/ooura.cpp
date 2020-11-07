#include "../fft_JniFft.h"
#include "ooura.h"
#include "fftsg.c"

#define LIBNAME "ooura"

#ifdef _DEBUG
#if defined(WIN32) || defined (WIN64)
#define log(msg, ...) printf(msg, __VA_ARGS__)
#else
#include <android/log.h>
#define log(msg) __android_log_print(ANDROID_LOG_VERBOSE, LIBNAME, msg)
#endif
#else
#define log(msg)
#endif

#if defined(WIN32) || defined (WIN64)
typedef unsigned __int64 LONGTYPE;
#else
typedef int LONGTYPE;
#endif

using namespace std;

ooura::ooura(int size) :  _size(size) {
    _data = make_unique<double[]>(static_cast<LONGTYPE>(size) + 1);

    int sizeSqrt = (int) (sqrt((double) _size) + 0.5) + 2;
    _ip = make_unique<int[]>(sizeSqrt);
    _ip.get()[0] = 0;

    _w = make_unique<double[]>(size);
}

void ooura::fft() {
    log("fft\n");

    rdft(_size, 1, _data.get(), _ip.get(), _w.get());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_fft_JniFft_internalNew(JNIEnv *env, jobject thiz, jint size) {
    ooura *o = new ooura(static_cast<int>(size));
    return reinterpret_cast<jlong>(o);
}

extern "C"
JNIEXPORT void JNICALL
Java_fft_JniFft_internalCalculate(JNIEnv *env, jobject thiz, jdoubleArray inputData, jdoubleArray outputData, jint cutOff, jlong nativeObjectPointer) {
    ooura *o = reinterpret_cast<ooura *>(nativeObjectPointer);
    env->GetDoubleArrayRegion(inputData,0, o->size(), o->data());
    o->fft();
    env->SetDoubleArrayRegion(outputData,0, cutOff * 2, o->data());
}

extern "C"
JNIEXPORT void JNICALL
Java_fft_JniFft_internalDispose(JNIEnv *env, jobject thiz, jlong nativeObjectPointer) {
    ooura *o = reinterpret_cast<ooura *>(nativeObjectPointer);
    delete o;
}