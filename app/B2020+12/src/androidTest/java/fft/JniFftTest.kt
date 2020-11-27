package fft

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.lang.Math.*

class JniFftTest {
    private lateinit var fft: JniFft
    private lateinit var input: DoubleArray
    private lateinit var output: DoubleArray

    private val delta = 0.001
    private val fftSize = 8192
    private val cutOff = 100

    @Before
    fun setUp() {
        fft = JniFft(fftSize)

        input = DoubleArray(fftSize)
        output = DoubleArray(2 * cutOff)
    }

    @After
    fun tearDown() {
        fft.dispose()
    }

    @Test
    fun fft_sin_frequency1() {
        for (i in input.indices) {
            input[i] = sin(2 * PI * i / input.size)
        }

        fft.calculate(input, output, cutOff)

        Assert.assertEquals(0.0, output[0] / (fftSize/2), delta)
        Assert.assertEquals(0.0, output[1] / (fftSize/2), delta)
        Assert.assertEquals(0.0, output[2] / (fftSize/2), delta)
        Assert.assertEquals(1.0, output[3] / (fftSize/2), delta)
    }

    @Test
    fun fft_cos_frequency1() {
        for (i in input.indices) {
            input[i] = cos(2 * PI * i / input.size)
        }

        fft.calculate(input, output, cutOff)

        Assert.assertEquals(0.0, output[0] / (fftSize/2), delta)
        Assert.assertEquals(0.0, output[1] / (fftSize/2), delta)
        Assert.assertEquals(1.0, output[2] / (fftSize / 2), delta)
        Assert.assertEquals(0.0, output[3] / (fftSize/2), delta)
    }
}