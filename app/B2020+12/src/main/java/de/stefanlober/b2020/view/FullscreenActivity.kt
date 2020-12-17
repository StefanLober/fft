package de.stefanlober.b2020.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
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
    private lateinit var chartView: ChartView
    private lateinit var audioController: AudioController
    private lateinit var fft: IFft
    private lateinit var fftWrapper: FftWrapper
    private val fftSize = 8192
    private val cutOff = 190

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.getLogger("B2020Logger").log(Level.INFO, "onCreate")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions,0)
        }

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        chartView = findViewById(R.id.chart_view)
        setMargin(resources.configuration.orientation)

        fft = JniFft(fftSize)
        fftWrapper = FftWrapper(fft, fftSize, cutOff)

        audioController = AudioController(this, fftWrapper)
    }

    override fun onResume() {
        super.onResume()
        Logger.getLogger("B2020Logger").log(Level.INFO, "onResume")

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);

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

        setMargin(newConfig.orientation)
    }

    private fun setMargin(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            chartView.xMarginFraction = ChartView.portraitXMargin
            chartView.yMarginFraction = ChartView.portraitYMargin
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartView.xMarginFraction = ChartView.landscapeXMargin
            chartView.yMarginFraction = ChartView.landscapeYMargin
        }
    }

    override fun update(dataList: ArrayList<DoubleArray>) {
        chartView.post {
            chartView.setData(dataList)
            chartView.invalidate()
        }
    }
}