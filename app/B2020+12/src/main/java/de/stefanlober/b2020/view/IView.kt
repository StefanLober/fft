package de.stefanlober.b2020.view

interface IView {
    fun update(data: DoubleArray)
    fun setAudioParams(sampleRate: Int, minBufferSize: Int)
}