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

    @Before
    fun setUp() {
        fft = TestFft()
        fftWrapper = FftWrapper(fft, fftSize, cutOff)
    }

    @After
    fun tearDown() {
        fft.dispose()
    }

    @Test
    fun fft_sin_frequency1() {
        val data = ShortArray(dataSize)
        for (i in data.indices) {
            data[i] = (amplitude * Math.sin(2 * Math.PI * i / fftSize)).toShort()
        }

        var scaledOutput = fftWrapper.calculate(data)

        Assert.assertEquals(0.0, scaledOutput[0], delta)
        Assert.assertEquals(1.0, scaledOutput[1], delta)
    }
}

class TestFft : IFft {
    override fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int) {
        outputData[0] = 0.0
        outputData[1] = 0.0
        outputData[2] = 0.0
        outputData[3] = (inputData.size/2).toDouble()
    }

    override fun dispose() {
    }
}