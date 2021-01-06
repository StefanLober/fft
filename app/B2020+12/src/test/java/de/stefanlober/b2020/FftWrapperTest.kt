package de.stefanlober.b2020

import fft.IFft
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FftWrapperTest {
    private lateinit var fft: TestFft
    private lateinit var fftWrapper: FftWrapper

    private val delta = 0.001
    private val fftSize = 8192
    private val cutOff = 100
    private val dataSize = 2500
    private val amplitude = 3000

    private var outputValues = DoubleArray(4)

    @Before
    fun setUp() {
        fft = TestFft(outputValues)
        fftWrapper = FftWrapper(fft, fftSize, cutOff,)
    }

    @After
    fun tearDown() {
        fft.dispose()
    }

    @Test
    fun fft_sin_frequency1_amp1() {
        val data = ShortArray(dataSize)
        for (i in data.indices) {
            data[i] = (amplitude * Math.sin(2 * Math.PI * i / dataSize)).toShort()
        }

        outputValues[3] = (fftSize / 2).toDouble()

        var scaledOutput = fftWrapper.calculate(data, false, false)

        Assert.assertEquals(0.0, scaledOutput[0], delta)
        Assert.assertEquals(1.0, scaledOutput[1], delta)
    }

    @Test
    fun fft_sin_frequency1_amp1_logY() {
        val data = ShortArray(dataSize)
        for (i in data.indices) {
            data[i] = (amplitude * Math.sin(2 * Math.PI * i / dataSize)).toShort()
        }

        outputValues[3] = (fftSize / 2).toDouble()

        var scaledOutput = fftWrapper.calculate(data, false, true)

        Assert.assertEquals(0.0, scaledOutput[0], delta)
        Assert.assertEquals(0.0, scaledOutput[1], delta)
    }

    @Test
    fun fft_sin_frequency1_max_logY() {
        val data = ShortArray(dataSize)
        for (i in data.indices) {
            data[i] = (amplitude * Math.sin(2 * Math.PI * i / dataSize)).toShort()
        }

        outputValues[3] = fftWrapper.maxY * fftSize / 2

        var scaledOutput = fftWrapper.calculate(data, false, true)

        Assert.assertEquals(0.0, scaledOutput[0], delta)
        Assert.assertEquals(fftWrapper.maxY, scaledOutput[1], delta)
    }
}

class TestFft(private var outputValues: DoubleArray) : IFft {
    override fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int) {
        outputData[0] = outputValues[0]
        outputData[1] = outputValues[1]
        outputData[2] = outputValues[2]
        outputData[3] = outputValues[3]
    }

    override fun dispose() {
    }
}