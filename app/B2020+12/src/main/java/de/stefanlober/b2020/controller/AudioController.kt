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
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val channel = AudioFormat.CHANNEL_IN_MONO
    private val sampleRate = 22050

    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private lateinit var data: ShortArray
    private lateinit var processBuffer: ShortArray

    private val processExecutor = Executors.newSingleThreadExecutor(PriorityThreadFactory(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO))

    fun start() {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            if(audioRecorder == null) {
                audioRecorder = AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(encoding)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channel)
                            .build()
                    ).build()
            }
            else {
                audioRecorder?.stop()
            }

            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
            view.setAudioParams(sampleRate, minBufferSize)
            data = ShortArray(minBufferSize)
            processBuffer = ShortArray(minBufferSize)
            audioRecorder?.startRecording()
            isActive = true

            read()
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "start", ex)
        }
    }

    fun stop() {
        try {
            audioRecorder?.stop()
            isActive = false
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "stop", ex)
        }
    }

    fun cleanUp() {
        try {
            audioRecorder?.stop()
            audioRecorder?.release()
            audioRecorder = null
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "cleanUp", ex)
        }
    }

    private fun read() {
        Thread {
            run {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

                try {
                    while (isActive) {
                        val read = audioRecorder!!.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                        if (read > 0) {
                            Logger.getLogger("B2020Logger").log(Level.INFO,  (System.currentTimeMillis() % 3600000).toString() + " processData")
                            processData(data.copyInto(processBuffer))
                        }
                    }
                } catch (ex: Exception) {
                    Logger.getLogger("B2020Logger").log(Level.WARNING, "read", ex)
                }
            }
        }.start()
    }

    private fun processData(processBuffer: ShortArray) {
        processExecutor.submit {
            try {
                Logger.getLogger("B2020Logger").log(Level.INFO, (System.currentTimeMillis() % 3600000).toString() + " calculate")
                val scaledOutput = fftWrapper.calculate(processBuffer, true, true)

                Logger.getLogger("B2020Logger").log(Level.INFO, (System.currentTimeMillis() % 3600000).toString() + " view.update")
                view.update(scaledOutput)
            } catch (ex: Exception) {
                Logger.getLogger("B2020Logger").log(Level.WARNING, "callback", ex)
            }
        }
    }
}

private class PriorityThreadFactory(val priority: Int) : ThreadFactory {
    override fun newThread(runnable: Runnable?): Thread {
        val thread = Thread(runnable)
        android.os.Process.setThreadPriority(priority)
        return thread
    }
}