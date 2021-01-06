package de.stefanlober.b2020

import fft.IFft
import kotlin.math.log10
import kotlin.math.sqrt

class FftWrapper(private val fft: IFft, private val fftSize: Int, private val cutOff: Int, private val meanCount: Int, logYTarget: Double) {
    private var input = DoubleArray(fftSize)
    private var output = DoubleArray(2 * cutOff)
    private var scaledOutput = DoubleArray(cutOff / meanCount)
    private val logYScaleFactor = logYTarget / log10(logYTarget)

    fun calculate(data: ShortArray, logX: Boolean, logY: Boolean): DoubleArray {
        val scaleFactor = data.size / input.size.toDouble()

        for (i in input.indices) {
            val inputIndexDouble = i * scaleFactor
            val inputIndex = inputIndexDouble.toInt()
            if (inputIndex + 1 < data.size) {
                input[i] = data[inputIndex] * (1 + inputIndex - inputIndexDouble) + data[inputIndex + 1] * (inputIndexDouble - inputIndex)
            } else {
                input[i] = data[inputIndex].toDouble()
            }
        }

        fft.calculate(input, output, cutOff)

        for (i in 0 until scaledOutput.size) {
            var sum = 0.0
            for (offset in 0 until meanCount) {
                val index = 2 * (i * meanCount + offset)
                sum += sqrt(output[index] * output[index] + output[index + 1] * output[index + 1]) / (fftSize / 2)
            }
            scaledOutput[i] = sum / meanCount
        }

        if (logY) {
            for (i in 0 until scaledOutput.size) {
                if (scaledOutput[i] < 1.0) {
                    scaledOutput[i] = 1.0
                }
                scaledOutput[i] = logYScaleFactor * Math.log10(scaledOutput[i])
            }
        }

        return scaledOutput
    }
}