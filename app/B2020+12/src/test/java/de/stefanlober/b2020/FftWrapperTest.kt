package de.stefanlober.b2020

import fft.IFft
import org.junit.Assert
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter

class FftWrapperTest {
    private lateinit var fft: TestFft
    private lateinit var fftWrapper: FftWrapper

    private val minDelta = 100.0
    private val deltaFactor = 0.02
    private val fftSize = 8192
    private val cutOff = 720
    private val dataSize = 2500
    private val meanCount = 1
    private val logXMinOut = 0.01
    private val logXMaxIn = cutOff.toDouble()
    private val logXMaxOut = cutOff.toDouble()
    private val logYMindB = -50.0
    private val logYMaxdB = 0.0

    private val data = ShortArray(dataSize)
    private val outputValues = DoubleArray(2 * cutOff)

    @Before
    fun setUp() {
        fft = TestFft(outputValues)
        fftWrapper = FftWrapper(fft, fftSize, cutOff, meanCount, logXMinOut, logXMaxIn, logXMaxOut, logYMindB, logYMaxdB)
    }

    @After
    fun tearDown() {
        fft.dispose()
    }

    @Test
    fun linear_frequencies() {
        val factor = Short.MAX_VALUE.toDouble() / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = false
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies.csv")

        Assert.assertEquals(cutOff, scaledOutput.size)
        Assert.assertEquals(0.0, scaledOutput[0], minDelta)
        Assert.assertEquals(Short.MAX_VALUE.toDouble(), scaledOutput[cutOff - 1], minDelta + Short.MAX_VALUE * deltaFactor)
    }

    @Test
    fun linear_frequencies_logX() {
        val factor = Short.MAX_VALUE.toDouble() / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = true
        fftWrapper.logY = false
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logX.csv")

        Assert.assertEquals(cutOff, scaledOutput.size)
        Assert.assertEquals(0.0, scaledOutput[0], minDelta)
        Assert.assertEquals(Short.MAX_VALUE.toDouble(), scaledOutput[cutOff - 1], minDelta + Short.MAX_VALUE * deltaFactor)
    }

    @Test
    fun linear_frequencies_logY() {
        val factor = Short.MAX_VALUE.toDouble() / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = false
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logY.csv")

        Assert.assertEquals(cutOff, scaledOutput.size)
        //Assert.assertEquals(0.0, scaledOutput[0], minDelta)
        Assert.assertEquals(Short.MAX_VALUE.toDouble(), scaledOutput[cutOff - 1], minDelta + Short.MAX_VALUE * deltaFactor)
    }

    @Test
    fun linear_frequencies_logX_logY() {
        val factor = Short.MAX_VALUE.toDouble() / cutOff

        for (i in 0 until cutOff) {
            outputValues[2 * i] = (i * factor * fftSize / 2)
        }

        fftWrapper.logX = true
        fftWrapper.logY = true
        val scaledOutput = fftWrapper.calculate(data)

        writeCsv(scaledOutput, "linear_frequencies_logX_logY.csv")

        Assert.assertEquals(cutOff, scaledOutput.size)
        //Assert.assertEquals(0.0, scaledOutput[0], minDelta)
        Assert.assertEquals(Short.MAX_VALUE.toDouble(), scaledOutput[cutOff - 1], minDelta + Short.MAX_VALUE * deltaFactor)
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