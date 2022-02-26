package de.stefanlober.b2020.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Process
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.max

class ChartSurfaceView : SurfaceView, SurfaceHolder.Callback {
    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    companion object {
        const val portraitXMargin = 0.01F
        const val portraitYMargin = 0.1F
        const val portraitListSize = 100

        const val landscapeXMargin = 0.02F
        const val landscapeYMargin = 0.05F
        const val landscapeListSize = 45
    }

    private val valueDivisor = 40F
    private val maxValueFactor = 10F

    private var dataTime = 80
    private var frameTime = 16

    private var maxValueStepCount = 300

    private var dataTimeNs = 80 * 1000000L

    private val lock: Any = Any()
    var listSize = landscapeListSize

    private var stepHeight: Float = 0F
    private var canvasBitmap: Bitmap? = null

    private var paint: Paint = Paint()
    private var erasePaint: Paint = Paint()

    private var path = Path()

    private var threadRunning = false
    private var bitmapCanvas: Canvas? = null

    lateinit var xScaleChange: (() -> Unit)
    lateinit var yScaleChange: (() -> Unit)

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        paint.color = Color.DKGRAY
        paint.strokeWidth = resources.displayMetrics.density
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        erasePaint.color = Color.WHITE
        erasePaint.style = Paint.Style.FILL
        //erasePaint.isAntiAlias = true

        isFocusable = false
        setZOrderOnTop(true)

        holder.addCallback(this)
        holder.setFormat(PixelFormat.RGB_565)

        setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        Logger.getLogger("B2020Logger").log(Level.INFO, (System.currentTimeMillis() % 3600000).toString() + " touch x=" + event.x + " y=" + event.y)
                        if (event.y < height / 3) {
                            swapColors()
                        } else if (event.x < width / 2) {
                            xScaleChange.invoke()
                        } else {
                            yScaleChange.invoke()
                        }
                    } catch (ex: Exception) {
                        Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " sleep", ex)
                    }
                }
            }

            v?.onTouchEvent(event) ?: true
        }
    }

    private fun swapColors() {
        val paintColor = paint.color
        paint.color = erasePaint.color
        erasePaint.color = paintColor
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        threadRunning = true

        Thread {
            var lastTimeNs = System.nanoTime()
            Process.setThreadPriority(-10)

            stepHeight = height / listSize.toFloat()

            run {
                try {
                    while (threadRunning) {
                        val timeNs = System.nanoTime()
                        val deltaTimeNs = timeNs - lastTimeNs
                        lastTimeNs = timeNs

                        stepHeight = (height / listSize).toFloat()
                        val translateY = -stepHeight * deltaTimeNs / dataTimeNs
                        //Logger.getLogger("B2020Logger").log(Level.INFO, System.currentTimeMillis().toString() + " translateY: " + translateY)

                        calculateMargins()

                        synchronized(lock) {
                            bitmapCanvas!!.drawBitmap(canvasBitmap!!, 0F, translateY, erasePaint)
                            bitmapCanvas!!.drawRect(0F, bitmapCanvas!!.height + translateY, bitmapCanvas!!.width.toFloat(), bitmapCanvas!!.height.toFloat(), erasePaint)
                        }

                        val canvas = holder.lockHardwareCanvas()
                        //canvas?.drawColor(erasePaint.color)
                        canvas?.drawBitmap(canvasBitmap!!, 0F, 0F, erasePaint)
                        holder.unlockCanvasAndPost(canvas)
                    }
                } catch (ex: Exception) {
                    Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " draw")
                }
            }
        }.start()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        threadRunning = false
    }

    fun setData(data: DoubleArray) {
        Logger.getLogger("B2020Logger").log(Level.INFO, (System.currentTimeMillis() % 3600000).toString() + " setData")

        drawData(data, bitmapCanvas)
    }

    private fun drawData(data: DoubleArray, canvas: Canvas?) {
        try {
            stepHeight = (height / listSize).toFloat()
            val yScaleFactor = maxValueStepCount * stepHeight / Short.MAX_VALUE

            path.reset()

            val yCenter = height - paint.strokeWidth
            path.moveTo(0F, yCenter)

            val xScaleFactor = width / data.size.toFloat()

            for (i in 1 until data.size - 1) {
                val xCoord = i.toFloat() * xScaleFactor
                val scaledValue = max((data[i] * yScaleFactor).toFloat(), 0F)
                val yCoord = yCenter - scaledValue
                path.lineTo(xCoord, yCoord)
            }

            //path.lineTo(canvasBitmap!!.width.toFloat(), yCenter)

            synchronized(lock) {
                canvas?.drawPath(path, erasePaint)
                canvas?.drawPath(path, paint)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, " drawData")
        }
    }

    private fun calculateMargins() {

        try {
            if (canvasBitmap == null || canvasBitmap!!.width != width || canvasBitmap!!.height != height) {
                canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                bitmapCanvas = Canvas(canvasBitmap!!)
                bitmapCanvas!!.drawColor(erasePaint.color)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " calculateMargins")
        }
    }

    fun setAudioParams(sampleRate: Int, minBufferSize: Int) {
        try {
            synchronized(holder) {
                dataTimeNs = (1000 * 1000000L * minBufferSize) / sampleRate
                Logger.getLogger("B2020Logger").log(Level.WARNING, " dataTime: " + dataTimeNs)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " setMinBufferSize")
        }
    }
}