#define _USE_MATH_DEFINES 1

#include <string>
#include <iostream>
#include <fstream>
#include <cmath>
#include <chrono>
#include <thread>
#include <vector>
#include <algorithm>

#include "ooura.h"

using namespace std;

const auto processor_count = thread::hardware_concurrency();
const int LENGTH = 0x1000;
const int CUT_OFF = 0x20;

void fft(ooura* ooura)
{
    ooura->fft();
}

int main()
{
    vector<thread> threads(processor_count);
    vector<unique_ptr<ooura>> oouras(processor_count);

    chrono::steady_clock::time_point begin = chrono::steady_clock::now();

    for (unsigned int i = 0; i < processor_count; i++)
    {
        oouras[i] = make_unique<ooura>(LENGTH);
        double* input = oouras[i].get()->data();
        for (int x = 0; x < LENGTH; x++)
        {
            input[x] = 2 * sin(x * (double)2 * M_PI / (double)LENGTH) + sin((double)2 * x * (double)2 * M_PI / (double)LENGTH);
        }
    }

    unique_ptr<double[]> inputPtr = make_unique<double[]>(LENGTH);
    double* input = inputPtr.get();
    copy_n(oouras[0].get()->data(), LENGTH, input);

    for (unsigned int i = 0; i < processor_count; i++)
    {
        threads[i] = thread(fft, oouras[i].get());
        threads[i].join();
    }

    chrono::steady_clock::time_point end = chrono::steady_clock::now();
    cout << "time for data of length " << LENGTH << ": " << chrono::duration_cast<chrono::milliseconds>(end - begin).count() << endl;

    ofstream resultFile("result.csv");

    double* output = oouras[0].get()->data();
    for (int x = 0; x < LENGTH; x++)
    {
        resultFile << input[x] << ";";
        if(x < 2 * CUT_OFF)
            resultFile << sqrt(output[x * 2] * output[x * 2] + output[x * 2 + 1] * output[x * 2 + 1]) * 2 / LENGTH;

        resultFile << endl;
    }

    resultFile.flush();
    resultFile.close();
}