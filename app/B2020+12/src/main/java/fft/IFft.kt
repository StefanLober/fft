package fft

interface IFft {
    fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int)

    fun dispose()
}
