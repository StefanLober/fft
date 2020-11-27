package de.stefanlober.b2020

import fft.IFft

class FftWrapper(private val fft: IFft, private val fftSize: Int, private val cutOff: Int) {
    private var input = DoubleArray(fftSize)
    private var output = DoubleArray(2 * cutOff)

    fun calculate(data: ShortArray): DoubleArray {
        val scaleFactor = data.size / input.size.toDouble()
        for (i in input.indices) {
            input[i] = data[(i * scaleFactor).toInt()].toDouble()
        }

        fft.calculate(input, output, cutOff)

        val scaledOutput = DoubleArray(cutOff)
        for (i in 0 until cutOff) {
            scaledOutput[i] = (Math.sqrt(output[2 * i] * output[2 * i] + output[2 * i + 1] * output[2 * i + 1]) / (fftSize / 2))
        }

        return scaledOutput
    }
}