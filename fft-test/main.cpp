#include "../fft_JniFft.h"
#include "ooura.h"

#define _USE_MATH_DEFINES 1

#include <string>
#include <iostream>
#include <fstream>
#include <cmath>
#include <chrono>
#include <thread>
#include <vector>

using namespace std;

const auto processor_count = thread::hardware_concurrency();
const int DATA_COUNT = 0x1000;
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
    vector<thread> threads(processor_count);
    vector<ThreadData> threadData(processor_count);
    
    chrono::steady_clock::time_point begin = chrono::steady_clock::now();

    for(unsigned int i=0; i<processor_count; i++)
    {
        threadData[i].input = new double[LENGTH];
        threadData[i].output = new double[LENGTH + 1];

        for (int x = 0; x < LENGTH; x++)
        {
            threadData[i].input[x] = (x < LENGTH / 2 ? 1 : 0) + sin(x * (double)2 * M_PI / (double)LENGTH);
        }

        threads[i] = thread(fft, &threadData[i]);
    }

    for (unsigned int i = 0; i < processor_count; i++)
    {
        threads[i].join();
    }

    chrono::steady_clock::time_point end = chrono::steady_clock::now();
    cout << "time for " << DATA_COUNT << " data sets of length " << LENGTH << ": " << chrono::duration_cast<chrono::milliseconds>(end - begin).count() << endl;

    ofstream resultFile("result.csv");
    for (int x = 0; x < LENGTH; x++)
    {
        resultFile << threadData[0].input[x] << ";";

        auto firstThreadDataOutput = threadData[0].output;
        if (x * 2 < CUT_OFF)
            resultFile << sqrt(firstThreadDataOutput[x * 2] * firstThreadDataOutput[x * 2] + firstThreadDataOutput[x * 2 + 1] * firstThreadDataOutput[x * 2 + 1]) * 2 / LENGTH << endl;
        else
            resultFile << endl;
    }
    resultFile.flush();
    resultFile.close();
}