package de.stefanlober.b2020.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
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
    private val meanCount = 1
    private val logXMinOut = 1.0
    private val logXMaxIn = cutOff.toDouble()
    private val logXMaxOut = cutOff.toDouble()
    private val logYMinIn = 1.0
    private val logYMaxIn = Short.MAX_VALUE.toDouble()
    private val logYMaxOut = Short.MAX_VALUE.toDouble()

    private lateinit var chartView: ChartSurfaceView
    private lateinit var audioController: AudioController
    private lateinit var fft: IFft
    private lateinit var fftWrapper: FftWrapper

    private val xScaleChange: () -> Unit = { fftWrapper.logX = !fftWrapper.logX }
    private val yScaleChange: () -> Unit = { fftWrapper.logY = !fftWrapper.logY }

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
        chartView.xScaleChange = xScaleChange
        chartView.yScaleChange = yScaleChange

        fft = JniFft(fftSize)
        fftWrapper = FftWrapper(fft, fftSize, cutOff, meanCount, logXMinOut, logXMaxIn, logXMaxOut, logYMinIn, logYMaxIn, logYMaxOut)

        audioController = AudioController(this, fftWrapper)
    }

    override fun onResume() {
        super.onResume()
        Logger.getLogger("B2020Logger").log(Level.INFO, "onResume")

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
    }

    override fun update(data: DoubleArray) {
        chartView.setData(data)
    }

    override fun setAudioParams(sampleRate: Int, minBufferSize: Int) {
        chartView.setAudioParams(sampleRate, minBufferSize)
    }
}