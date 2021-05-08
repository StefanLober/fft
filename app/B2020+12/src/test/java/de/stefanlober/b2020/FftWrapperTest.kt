package de.stefanlober.b2020

import fft.IFft
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter

class FftWrapperTest {
    private lateinit var fft: TestFft
    private lateinit var fftWrapper: FftWrapper

    private val delta = 10.0
    private val fftSize = 8192
    private val cutOff = 720
    private val dataSize = 2500
    private val amplitude = 800.0
    private val meanCount = 1
    private val logXMinY = 1.0
    private val logXMaxX = cutOff.toDouble()
    private val logXMaxY = cutOff.toDouble()
    private val logYMinX = 100.0
    private val logYMaxX = Short.MAX_VALUE.toDouble()
    private val logYMaxY = Short.MAX_VALUE.toDouble()

    private val data = ShortArray(dataSize)
    private val outputValues = DoubleArray(2 * cutOff)

    @Before
    fun setUp() {
        fft = TestFft(outputValues)
        fftWrapper = FftWrapper(fft, fftSize, cutOff, meanCount, logXMinY, logXMaxX, logXMaxY, logYMinX, logYMaxX, logYMaxY)
    }

    @After
    fun tearDown() {
        fft.dispose()
    }

//    @Test
//    fun const_amplitude() {
//        outputValues[0] = amplitude * fftSize
//
//        fftWrapper.logX = false
//        fftWrapper.logY = false
//        val scaledOutput = fftWrapper.calculate(data)
//
//        Assert.assertEquals(amplitude * 2.0, scaledOutput[0], delta)
//        Assert.assertEquals(0.0, scaledOutput[1], delta)
//    }

    @Test
    fun sin_frequency1() {
        outputValues[3] = amplitude * fftSize / 2

        fftWrapper.logX = false
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun sin_frequency1_logX() {
        outputValues[3] = amplitude * fftSize / 2

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "sin_frequency1_logX.csv")

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun sin_frequency1_logY() {
        outputValues[3] = amplitude * fftSize / 2

        fftWrapper.logX = false
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "sin_frequency1_logY.csv")

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun sin_frequency1_logX_logY() {
        outputValues[3] = amplitude * fftSize / 2

        fftWrapper.logX = true
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "sin_frequency1_logX_logY.csv")

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun sin_frequencyMaxX_logX() {
        outputValues[2 * logXMaxX.toInt() - 2] = amplitude * fftSize / 2

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "sin_frequencyMaxX_logX.csv")

        Assert.assertEquals(amplitude, scaledOutput[logXMaxY.toInt() - 1], delta)
    }

    @Test
    fun all_frequencies() {
        for (i in 0 until cutOff) {
            outputValues[2 * i] = amplitude * fftSize / 2
        }

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "all_frequencies.csv")

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun all_frequencies_logX() {
        for (i in 0 until cutOff) {
            outputValues[2 * i] = amplitude * fftSize / 2
        }

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "all_frequencies_logX.csv")

        Assert.assertEquals(amplitude, scaledOutput[0], delta)
    }

    @Test
    fun all_frequencies_logY() {
        for (i in 0 until cutOff) {
            outputValues[2 * i] = amplitude * fftSize / 2
        }

        fftWrapper.logX = false
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "all_frequencies_logY.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
//        Assert.assertEquals(logYTarget, scaledOutput[1], delta)
    }

    @Test
    fun all_frequencies_logX_logY() {
        for (i in 0 until cutOff) {
            outputValues[2 * i] = amplitude * fftSize / 2
        }

        fftWrapper.logX = true
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "all_frequencies_logX_logY.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
//        Assert.assertEquals(logYTarget, scaledOutput[1], delta)
    }

    @Test
    fun linear_frequencies() {
        val factor = logYMaxX / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = false
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
//        Assert.assertEquals(logYTarget, scaledOutput[1], delta)
    }

    @Test
    fun linear_frequencies_logX() {
        val factor = logYMaxX / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logX.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
//        Assert.assertEquals(logYTarget, scaledOutput[1], delta)
    }

    @Test
    fun linear_frequencies_logY() {
        val factor = logYMaxX / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = false
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logY.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
        Assert.assertEquals(logYMaxY, scaledOutput[cutOff - 1], delta)
    }

    @Test
    fun linear_frequencies_logX_logY() {
        val factor = logYMaxX / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = true
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logX_logY.csv")

//        Assert.assertEquals(0.0, scaledOutput[0], delta)
//        Assert.assertEquals(logYTarget, scaledOutput[1], delta)
    }

    private fun writeCsv(values: DoubleArray, fileName: String) {
        try {
            val directoryName = "output"
            val directory = File(directoryName)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File("$directoryName/$fileName")
            PrintWriter(file).use { writer ->
                for(value in values) {
                    writer.write(value.toString() + "\n")
                }
            }
        } catch (ex: FileNotFoundException) {
            System.out.println(ex.message)
        }
    }
}

class TestFft(private var outputValues: DoubleArray) : IFft {
    override fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int) {
        System.arraycopy(outputValues, 0, outputData, 0, outputData.size)
    }

    override fun dispose() {
    }
}