#include "../fft_JniFft.h"
#include "ooura.h"

#define _USE_MATH_DEFINES 1

#include <string>
#include <iostream>
#include <fstream>
#include <memory>
#include <cmath>
#include <chrono>
#include <thread>

using namespace std;

const auto processor_count = thread::hardware_concurrency();
const int DATA_COUNT = 0x10000;

const int LENGTH = 0x4000;
const int CUT_OFF = 0x80;

struct ThreadData {
    double* input;
    double* output;
};

void fft(ThreadData* threadData)
{
    for (unsigned int i = 0; i < DATA_COUNT / processor_count; i++)
    {
        ooura_fft(threadData->input, threadData->output, LENGTH);
    }
}

int main()
{
    unique_ptr<thread> threads(new thread[processor_count]);
    unique_ptr<ThreadData> threadDataPtr(new ThreadData[processor_count]);
    
    chrono::steady_clock::time_point begin = chrono::steady_clock::now();

    for(unsigned int i=0; i<processor_count; i++)
    {
        threadDataPtr.get()[i].input = new double[LENGTH];
        threadDataPtr.get()[i].output = new double[LENGTH + 1];

        for (int x = 0; x < LENGTH; x++)
        {
            threadDataPtr.get()[i].input[x] = (x < LENGTH / 2 ? 1 : 0) + sin(x * (double)2 * M_PI / (double)LENGTH);
        }

        threads.get()[i] = thread(fft, threadDataPtr.get());
    }

    for (unsigned int i = 0; i < processor_count; i++)
    {
        threads.get()[i].join();
    }

    chrono::steady_clock::time_point end = chrono::steady_clock::now();
    cout << "time for " << DATA_COUNT << " data sets of length " << LENGTH << ": " << chrono::duration_cast<chrono::milliseconds>(end - begin).count() << endl;

    ofstream resultFile("result.csv");
    for (int x = 0; x < LENGTH; x++)
    {
        resultFile << threadDataPtr.get()[0].input[x] << ";";

        auto firstThreadDataOutput = threadDataPtr.get()[0].output;
        if (x * 2 < CUT_OFF)
            resultFile << sqrt(firstThreadDataOutput[x * 2] * firstThreadDataOutput[x * 2] + firstThreadDataOutput[x * 2 + 1] * firstThreadDataOutput[x * 2 + 1]) * 2 / (LENGTH) << endl;
        else
            resultFile << endl;
    }
    resultFile.flush();
    resultFile.close();
}