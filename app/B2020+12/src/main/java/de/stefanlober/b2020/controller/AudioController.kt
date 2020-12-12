package de.stefanlober.b2020.controller

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import de.stefanlober.b2020.FftWrapper
import de.stefanlober.b2020.view.IView
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.logging.Level
import java.util.logging.Logger

class AudioController(private var view: IView, private var fftWrapper: FftWrapper) {
    private var lastUpdateTime: Long = System.currentTimeMillis()
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val channel = AudioFormat.CHANNEL_IN_MONO
    private val sampleRate = 44100

    private val listSize = 50

    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private lateinit var data: ShortArray
    private val dataList: ArrayList<DoubleArray> = ArrayList()

    fun init() {
        try {
            audioRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channel)
                        .build()
                )
                .build()
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "init")
        }
    }

    fun start() {
        try {
            data = ShortArray(AudioRecord.getMinBufferSize(sampleRate, channel, encoding))
            audioRecorder?.startRecording()
            isActive = true

            read()
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "start")
        }
    }

    fun stop() {
        try {
            audioRecorder?.stop()
            isActive = false
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "stop")
        }
    }

    fun cleanUp() {
        try {
            audioRecorder?.stop()
            audioRecorder?.release()
            audioRecorder = null
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "cleanUp")
        }
    }

    private fun read() {
        Executors.newSingleThreadExecutor(PriorityThreadFactory(Thread.NORM_PRIORITY + 1)).execute {
            try {
                while (isActive) {
                    val read = audioRecorder!!.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        //Logger.getLogger("B2020Logger").log(Level.INFO,  System.currentTimeMillis().toString() + " processData " + Thread.currentThread().name)
                        processData(data)
                    }
                }
            } catch (ex: Exception) {
                Logger.getLogger("B2020Logger").log(Level.WARNING, "read")
            }
        }
    }

    private fun processData(data: ShortArray) {
        Executors.newSingleThreadExecutor(PriorityThreadFactory(Thread.NORM_PRIORITY + 1)).execute {
            try {
                //Logger.getLogger("B2020Logger").log(Level.INFO,  System.currentTimeMillis().toString() + " calculate " + Thread.currentThread().name)
                val scaledOutput = fftWrapper.calculate(data)

                dataList.add(0, scaledOutput)
                while (dataList.size > listSize)
                    dataList.removeAt(listSize)

                Logger.getLogger("B2020Logger").log(Level.INFO,  (System.currentTimeMillis() % 3600000).toString() + " view.update ")
                view.update(dataList)
            } catch (ex: Exception) {
                Logger.getLogger("B2020Logger").log(Level.WARNING, "callback", ex)
            }
        }
    }

    private class PriorityThreadFactory(val priority: Int) : ThreadFactory {
        override fun newThread(runnable: Runnable): Thread {
            var thread = Thread(runnable)
            thread.priority = priority
            return thread
        }
    }
}