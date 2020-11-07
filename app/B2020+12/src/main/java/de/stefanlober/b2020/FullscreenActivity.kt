package de.stefanlober.b2020

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors


class FullscreenActivity() : AppCompatActivity() {
    private lateinit var chartView: ChartView
    private lateinit var fullscreenContent: TextView
    private lateinit var threadPool: Unit
    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private var _data: ShortArray? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fullscreenContent = findViewById(R.id.fullscreen_content)
        chartView = findViewById(R.id.chart_view)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        supportActionBar?.hide()

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }

        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val channel = AudioFormat.CHANNEL_IN_MONO
        val sampleRate = 44100

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
        _data = ShortArray(AudioRecord.getMinBufferSize(sampleRate, channel, encoding))

        audioRecorder!!.startRecording()
        isActive = true

        read(Handler(Looper.getMainLooper()), callback)
    }

    private fun read(resultHandler: Handler, callback: (ShortArray) -> Unit) {
        threadPool = Executors.newSingleThreadExecutor { task -> Thread(task, "audio-thread")
        }.execute {
            try {
                while (isActive) {
                    val read = audioRecorder!!.read(_data!!, 0, _data!!.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        resultHandler.post { callback(_data!!) }

                        Thread.sleep(50)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private val callback: (ShortArray) -> Unit = {
            data ->

        try {
//            var sum = 0
//            for (index in 0..data.size-1 step 2) {
//                var value: Int = data[index + 1].toInt()
//                //if (value < 0) value *= -1
//                value = value shl 8
//                value += data[index].toInt()
//                sum += Math.abs(value)
//            }
//
//            var mean = sum / data.size
//            fullscreenContent.setText("Mean: $mean")

            chartView.setData(data)
            chartView.invalidate()
        }
        catch(ex: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        audioRecorder?.stop()
        audioRecorder?.release()
        audioRecorder = null

        _data = null
    }
}