package de.stefanlober.b2020

import fft.IFft
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class FftWrapper(private val fft: IFft, private val fftSize: Int, private val cutOff: Int, private val meanCount: Int,
                 logXMinY: Double, logXMaxX: Double, logXMaxY: Double,
                 logYMinX: Double, logYMaxX: Double, logYMaxY: Double) {
    private val input = DoubleArray(fftSize)
    private val output = DoubleArray(2 * cutOff)
    private val scaledOutput = DoubleArray(cutOff / meanCount)
    private val scaledOutputCopy = DoubleArray(cutOff / meanCount)
    private val logXA = logXMinY
    private val logXB = log10(logXMaxY / logXMinY) / logXMaxX
    private val logYA = logYMaxX / log10(logYMaxX / logYMinX)
    private val logYB = 1 / logYMinX

    var logX: Boolean = true
    var logY: Boolean = true

    fun calculate(data: ShortArray): DoubleArray {
        val scaleFactor = data.size / input.size.toDouble()

        for (i in input.indices) {
            val inputIndexDouble = i * scaleFactor
            val inputIndex = inputIndexDouble.toInt()
            if (inputIndex + 1 < data.size) {
                input[i] = data[inputIndex] * (1.0 + inputIndex.toDouble() - inputIndexDouble) + data[inputIndex + 1] * (inputIndexDouble - inputIndex.toDouble())
            } else {
                input[i] = data[inputIndex].toDouble()
            }
        }

        fft.calculate(input, output, cutOff)

        scaledOutput[0] = abs(output[0]) / fftSize

        for (i in 1 until scaledOutput.size) {
//            var sum = 0.0
//            for (offset in 0 until meanCount) {
//                val index = 2 * (i * meanCount + offset)
//                sum += sqrt(output[index] * output[index] + output[index + 1] * output[index + 1]) / (fftSize / 2)
//            }
//            scaledOutput[i] = sum / meanCount

            val index = 2 * i
            scaledOutput[i] = sqrt(output[index] * output[index] + output[index + 1] * output[index + 1]) / (fftSize / 2)
        }

        if (logY) {
            for (i in 0 until scaledOutput.size) {
                if (scaledOutput[i] < 1.0) {
                    scaledOutput[i] = 1.0
                }
                scaledOutput[i] = logYA * log10(logYB * scaledOutput[i])
            }
        }

        if (logX) {
            System.arraycopy(scaledOutput, 0, scaledOutputCopy, 0, scaledOutput.size)

            for (i in 0 until scaledOutputCopy.size) {
                val indexDouble = logXA * 10.0.pow(logXB * i.toDouble())
                val index = indexDouble.toInt()
                if (index + 1 < scaledOutput.size) {
                    scaledOutput[i] = scaledOutputCopy[index] * (1.0 + index.toDouble() - indexDouble) + scaledOutputCopy[index + 1] * (indexDouble - index.toDouble())
                } else if (index < scaledOutput.size) {
                    scaledOutput[i] = scaledOutputCopy[index]
                }
            }
        }

        return scaledOutput
    }
}