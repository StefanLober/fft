package de.stefanlober.b2020.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.stefanlober.b2020.FftWrapper
import de.stefanlober.b2020.R
import de.stefanlober.b2020.controller.AudioController
import fft.IFft
import fft.JniFft
import java.util.logging.Level
import java.util.logging.Logger

class FullscreenActivity : AppCompatActivity(), IView {
    private val fftSize = 8192
    private val cutOff = 720
    private val meanCount = 3
    private val logXMinOut = 0.1
    private val logXMaxIn = cutOff.toDouble()
    private val logXMaxOut = cutOff.toDouble()
    private val logYMindB = -65.0
    private val logYMaxdB = 1000.0

    private lateinit var chartView: ChartSurfaceView
    private lateinit var textViewX: TextView
    private lateinit var textViewY: TextView
    private lateinit var audioController: AudioController
    private lateinit var fft: IFft
    private lateinit var fftWrapper: FftWrapper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.getLogger("B2020Logger").log(Level.INFO, "onCreate")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        chartView = findViewById(R.id.chart_view)

        textViewX = findViewById(R.id.textView_x)
        textViewX.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        fftWrapper.logX = !fftWrapper.logX
                        if(fftWrapper.logX)
                            textViewX.text = "log\nX"
                        else
                            textViewX.text = "lin\nX"
                    } catch (ex: Exception) {
                        Logger.getLogger("B2020Logger").log(Level.WARNING," MotionEvent.ACTION_DOWN", ex)
                    }
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        textViewY = findViewById(R.id.textView_y)
        textViewY.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        fftWrapper.logY = !fftWrapper.logY
                        if(fftWrapper.logY)
                            textViewY.text = "log\nY"
                        else
                            textViewY.text = "lin\nY"
                    } catch (ex: Exception) {
                        Logger.getLogger("B2020Logger").log(Level.WARNING," MotionEvent.ACTION_DOWN", ex)
                    }
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        fft = JniFft(fftSize)
        fftWrapper = FftWrapper(fft, fftSize, cutOff, meanCount, logXMinOut, logXMaxIn, logXMaxOut, logYMindB, logYMaxdB)

        audioController = AudioController(this, fftWrapper)
    }

    override fun onResume() {
        super.onResume()
        Logger.getLogger("B2020Logger").log(Level.INFO, "onResume")

        applyOrientation(getResources().getConfiguration().orientation)

        audioController.start()
    }

    override fun onStop() {
        super.onStop()
        Logger.getLogger("B2020Logger").log(Level.INFO, "onStop")

        audioController.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.getLogger("B2020Logger").log(Level.INFO, "onDestroy")

        audioController.cleanUp()
        fft.dispose()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        applyOrientation(newConfig.orientation)
    }

    private fun applyOrientation(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            chartView.listSize = ChartSurfaceView.portraitListSize
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartView.listSize = ChartSurfaceView.landscapeListSize
        }
    }

    override fun update(data: DoubleArray) {
        chartView.setData(data)
    }

    override fun setAudioParams(sampleRate: Int, minBufferSize: Int) {
        chartView.setAudioParams(sampleRate, minBufferSize)
    }
}