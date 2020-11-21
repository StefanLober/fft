package fft

class JniFft(length: Int) {
    companion object {
        init {
            System.loadLibrary("ooura")
        }
    }

    private val nativeObjectPointer: Long

    private external fun internalNew(size: Int): Long
    private external fun internalCalculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int, nativeObjectPointer: Long)
    private external fun internalDispose(nativeObjectPointer: Long)

    fun calculate(inputData: DoubleArray, outputData: DoubleArray, cutOff: Int) {
        internalCalculate(inputData, outputData, cutOff, nativeObjectPointer)
    }

    fun dispose() {
        internalDispose(nativeObjectPointer)
    }

    init {
        nativeObjectPointer = internalNew(length)
    }
}