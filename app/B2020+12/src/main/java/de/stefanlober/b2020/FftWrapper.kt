package de.stefanlober.b2020

import fft.IFft
import java.lang.Double.doubleToLongBits
import java.lang.Double.max
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class FftWrapper(private val fft: IFft, private val fftSize: Int, private val cutOff: Int, private val meanCount: Int,
                 logXMinOut: Double, logXMaxIn: Double, logXMaxOut: Double,
                 private val logYMindB: Double, private val logYMaxdB: Double) {
    private val input = DoubleArray(fftSize)
    private val output = DoubleArray(2 * cutOff)
    private val scaledOutput = DoubleArray(cutOff / meanCount)
    private val scaledOutputCopy = DoubleArray(cutOff / meanCount)
    private val logXA = logXMinOut
    private val logXB = log10(logXMaxOut / logXMinOut) / logXMaxIn

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

        if (logX) {
            System.arraycopy(scaledOutput, 0, scaledOutputCopy, 0, scaledOutput.size)

            for (i in scaledOutputCopy.indices) {
                val indexDouble = logXA * 10.0.pow(logXB * i)
                val index = indexDouble.toInt()
                val valueAtIndex = scaledOutputCopy[index]
                if (index + 1 < scaledOutput.size) {
                    scaledOutput[i] = valueAtIndex + (scaledOutputCopy[index + 1] - valueAtIndex) * (indexDouble - index)
                } else if (index < scaledOutput.size) {
                    scaledOutput[i] = valueAtIndex
                }
            }
        }

        if (logY) {
            val minLogInput = 0.01
            for (i in scaledOutput.indices) {
                if (scaledOutput[i] < minLogInput) {
                    scaledOutput[i] = minLogInput
                }
                val dB = 20 * log10(scaledOutput[i] / Short.MAX_VALUE)
                scaledOutput[i] = (dB - logYMindB) * Short.MAX_VALUE / (logYMaxdB - logYMindB)
            }
        }

        return scaledOutput
    }
}