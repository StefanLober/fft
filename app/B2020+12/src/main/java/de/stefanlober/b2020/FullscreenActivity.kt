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
import java.util.logging.Level
import java.util.logging.Logger

class FullscreenActivity : AppCompatActivity() {
    private lateinit var chartView: ChartView
    private lateinit var fullscreenContent: TextView
    private lateinit var threadPool: Unit
    private var isActive: Boolean = false

    private var audioRecorder: AudioRecord? = null
    private var _data: ShortArray? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.getLogger("B2020Logger").log(Level.INFO, "onCreate")

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fullscreenContent = findViewById(R.id.fullscreen_content)
        chartView = findViewById(R.id.chart_view)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        Logger.getLogger("B2020Logger").log(Level.INFO, "onPostCreate")

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
    }

    override fun onResume() {
        super.onResume()

        Logger.getLogger("B2020Logger").log(Level.INFO, "onResume")

        audioRecorder?.startRecording()
        isActive = true

        read(Handler(Looper.getMainLooper()))
    }

    override fun onStop() {
        super.onStop()

        Logger.getLogger("B2020Logger").log(Level.INFO, "onStop")

        audioRecorder?.stop()
        isActive = false
    }

     private fun read(resultHandler: Handler) {
        threadPool = Executors.newSingleThreadExecutor { task -> Thread(task, "audio-thread")
        }.execute {
            try {
                while (isActive) {
                    val read = audioRecorder!!.read(_data!!, 0, _data!!.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        resultHandler.post { callback(_data!!) }

                        Thread.sleep(15)
                    }
                }
            } catch (ex: Exception) {
            }
        }
    }

    private val callback: (ShortArray) -> Unit = { data ->
        try {
            chartView.setData(data)
            chartView.invalidate()
        }
        catch(ex: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Logger.getLogger("B2020Logger").log(Level.INFO, "onDestroy")

        audioRecorder?.stop()
        audioRecorder?.release()
        audioRecorder = null

        _data = null
    }
}