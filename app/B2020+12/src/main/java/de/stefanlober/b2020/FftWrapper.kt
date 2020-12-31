package de.stefanlober.b2020

import fft.IFft

class FftWrapper(private val fft: IFft, private val fftSize: Int, private val cutOff: Int) {
    private var input = DoubleArray(fftSize)
    private var output = DoubleArray(2 * cutOff)
    private var scaledOutput = DoubleArray(cutOff)

    fun calculate(data: ShortArray, logX: Boolean, logY: Boolean): DoubleArray {
        val scaleFactor = data.size / input.size.toDouble()

        for (i in input.indices) {
            val inputIndexDouble = i * scaleFactor
            val inputIndex = inputIndexDouble.toInt()
            if(inputIndex + 1 < data.size) {
                input[i] = data[inputIndex] * (1 + inputIndex - inputIndexDouble) + data[inputIndex + 1] * (inputIndexDouble - inputIndex)
            }
            else {
                input[i] = data[inputIndex].toDouble()
            }
        }

        fft.calculate(input, output, cutOff)

        for (i in 0 until cutOff) {
            scaledOutput[i] = (Math.sqrt(output[2 * i] * output[2 * i] + output[2 * i + 1] * output[2 * i + 1]) / (fftSize / 2))
        }

        return scaledOutput
    }
}