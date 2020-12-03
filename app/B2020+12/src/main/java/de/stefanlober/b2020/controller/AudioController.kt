package de.stefanlober.b2020.controller

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import de.stefanlober.b2020.FftWrapper
import de.stefanlober.b2020.view.IView
import fft.JniFft
import java.lang.Math.sqrt
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger

class AudioController(private var view: IView, private var fftWrapper: FftWrapper) {
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val channel = AudioFormat.CHANNEL_IN_MONO
    private val sampleRate = 48000

    private val listSize = 40

    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private lateinit var data: ShortArray
    private var dataList: ArrayList<DoubleArray> = ArrayList()

    private val handler = Handler(Looper.getMainLooper())

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
        Executors.newSingleThreadExecutor { task -> Thread(task, "audio-thread") }.execute {
            try {
                while (isActive) {
                    val read = audioRecorder!!.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        processData(data)
                    }
                }
            } catch (ex: Exception) {
                Logger.getLogger("B2020Logger").log(Level.WARNING, "read")
            }
        }
    }

    private fun processData(data: ShortArray) {
        Executors.newSingleThreadExecutor { task -> Thread(task, "process-thread") }.execute {
            try {
                val scaledOutput = fftWrapper.calculate(data)
                dataList.add(0, scaledOutput)
                while (dataList.size > listSize)
                    dataList.removeAt(listSize)

                handler.post { view.update(dataList) }
            } catch (ex: Exception) {
                Logger.getLogger("B2020Logger").log(Level.WARNING, "callback", ex)
            }
        }
    }
}