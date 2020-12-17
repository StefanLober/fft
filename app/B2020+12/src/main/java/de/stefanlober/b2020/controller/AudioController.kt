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

    private val listSize = 40

    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private lateinit var data: ShortArray
    private val dataList: ArrayList<DoubleArray> = ArrayList()

    fun start() {
        try {
            if(audioRecorder == null) {
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
            }

            data = ShortArray(AudioRecord.getMinBufferSize(sampleRate, channel, encoding))
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
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                    while (isActive) {
                        val read = audioRecorder!!.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                        if (read > 0) {
                            //Logger.getLogger("B2020Logger").log(Level.INFO,  System.currentTimeMillis().toString() + " processData " + Thread.currentThread().name)
                            processData(data)
                        }
                    }
                } catch (ex: Exception) {
                    Logger.getLogger("B2020Logger").log(Level.WARNING, "read", ex)
                }
            }
        }.start()
    }

    private fun processData(data: ShortArray) {
        Thread {
            run {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
                    //Logger.getLogger("B2020Logger").log(Level.INFO,  System.currentTimeMillis().toString() + " calculate " + Thread.currentThread().name)
                    val scaledOutput = fftWrapper.calculate(data)

                    dataList.add(0, scaledOutput)
                    while (dataList.size > listSize)
                        dataList.removeAt(listSize)

                    Logger.getLogger("B2020Logger").log(Level.INFO, (System.currentTimeMillis() % 3600000).toString() + " view.update ")
                    view.update(dataList)
                } catch (ex: Exception) {
                    Logger.getLogger("B2020Logger").log(Level.WARNING, "callback", ex)
                }
            }
        }.start()
    }
}