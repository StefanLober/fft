/*\\\ INCLUDE FILES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

#include <algorithm>
#include <string>

#include "../fft_JniFft.h"
#include "fftsg.c"

#define LIBNAME "ooura"

#ifdef _DEBUG
    #if defined(WIN32) || defined (WIN64)
        #define log(msg) printf(msg)
    #else
        #include <android/log.h>
        #define log(msg) __android_log_print(ANDROID_LOG_VERBOSE, LIBNAME, msg)
    #endif
#else
    #define log(msg)
#endif

using namespace std;

JNIEXPORT void ooura_fft(double* input, double* output, int length)
{
    log("ooura_fft\n");

    int lengthSqrt = (int)(sqrt((double)length) + 0.5) + 2;
    int* ip = new int[lengthSqrt];
    ip[0] = 0;

    int lengthW = (int)(5 * length / 4);
    double* w = new double[lengthW];

    copy_n(input, length, output);
    rdft(length, 1, output, ip, w);

    delete[]ip;
    delete[]w;

    log("ooura_fft end\n");
}

JNIEXPORT void JNICALL Java_fft_JniFft_internalCalculate
(JNIEnv* env, jobject obj, jdoubleArray inputArray, jdoubleArray outputArray)
{
    log("Java_fft_JniFft_internalCalculate\n");

    int length = env->GetArrayLength(inputArray);
    double* input = env->GetDoubleArrayElements(inputArray, 0);
    double* output = env->GetDoubleArrayElements(outputArray, 0);
    ooura_fft(input, output, length);

    if (outputArray != NULL)
    {
        env->SetDoubleArrayRegion(outputArray, 0, length, output);
    }

    env->ReleaseDoubleArrayElements(inputArray, input, 0);

    log("delete []output\n");
    delete[]output;

    log("Java_fft_JniFft_internalCalculate end\n");
}