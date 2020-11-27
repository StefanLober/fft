package fft

class JniFft(length: Int) : IFft {
    companion object {
        init {
            System.loadLibrary("ooura")
        }
    }

    private val nativeObjectPointer: Long

    private external fun internalNew(size: Int): Long
    private external fun internalCalculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int, nativeObjectPointer: Long)
    private external fun internalDispose(nativeObjectPointer: Long)

    override fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int) {
        internalCalculate(inputData, outputData, cutOff, nativeObjectPointer)
    }

    override fun dispose() {
        internalDispose(nativeObjectPointer)
    }

    init {
        nativeObjectPointer = internalNew(length)
    }
}