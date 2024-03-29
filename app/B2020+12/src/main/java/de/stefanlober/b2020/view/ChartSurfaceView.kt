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

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    companion object {
        const val portraitListSize = 100

        const val landscapeListSize = 45
    }

    private var maxValueStepCount = 300

    private var translateYSum: Float = 0F

    private var dataTimeNs = 80 * 1000000L

    var listSize = landscapeListSize

    private var stepHeight: Float = 0F

    private var paint: Paint = Paint()
    private var erasePaint: Paint = Paint()

    private var path = Path()

    private var threadRunning = false

    var bitmapCanvas: Canvas? = null
    var canvasBitmap: Bitmap? = null

    var bitmapCanvas2: Canvas? = null
    var canvasBitmap2: Bitmap? = null

    private var bitmap2Active = false

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        paint.color = Color.rgb(70, 70, 70)
        paint.strokeWidth = resources.displayMetrics.density
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        erasePaint.color = Color.WHITE
        erasePaint.style = Paint.Style.FILL

        isFocusable = false
        setZOrderOnTop(true)

        holder.addCallback(this)
        holder.setFormat(PixelFormat.RGB_565)

        setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        swapColors()
                    } catch (ex: Exception) {
                        Logger.getLogger("B2020Logger").log(Level.WARNING," MotionEvent.ACTION_DOWN", ex)
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
                        createBitmap()

                        val timeNs = System.nanoTime()
                        val deltaTimeNs = timeNs - lastTimeNs
                        lastTimeNs = timeNs

                        val canvas = holder.lockHardwareCanvas()
                        try {
                            synchronized(holder) {
                                val translateY = -stepHeight * deltaTimeNs / dataTimeNs
                                //Logger.getLogger("B2020Logger").log(Level.INFO, ("translateY: " +  translateY))
                                translateYSum += translateY

                                if (bitmap2Active)
                                    canvas?.drawBitmap(canvasBitmap2!!, 0F, translateYSum, null)
                                else
                                    canvas?.drawBitmap(canvasBitmap!!, 0F, translateYSum, null)
                            }
                        } finally {
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                } catch (ex: Exception) {
                    Logger.getLogger("B2020Logger").log(Level.WARNING, " draw")
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
        synchronized(holder) {
            //Logger.getLogger("B2020Logger").log(Level.INFO, " setData; translateYSum: $translateYSum")
            if (bitmap2Active) {
                bitmapCanvas!!.drawBitmap(canvasBitmap2!!, 0F, translateYSum, null)
                bitmapCanvas!!.drawRect(0F,bitmapCanvas2!!.height + translateYSum, bitmapCanvas2!!.width.toFloat(), bitmapCanvas2!!.height.toFloat(), erasePaint)

                drawData(data, bitmapCanvas!!)
                canvasBitmap!!.prepareToDraw()
            } else {
                bitmapCanvas2!!.drawBitmap(canvasBitmap!!, 0F, translateYSum, null)
                bitmapCanvas2!!.drawRect(0F,bitmapCanvas!!.height + translateYSum, bitmapCanvas!!.width.toFloat(), bitmapCanvas!!.height.toFloat(), erasePaint)

                drawData(data, bitmapCanvas2!!)
                canvasBitmap2!!.prepareToDraw()
            }

            bitmap2Active = !bitmap2Active
            translateYSum = 0F
        }
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

            canvas?.drawPath(path, erasePaint)
            canvas?.drawPath(path, paint)
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, " drawData")
        }
    }

    private fun createBitmap() {
        try {
            if (canvasBitmap == null || canvasBitmap!!.width != width || canvasBitmap!!.height != height) {
                canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                bitmapCanvas = Canvas(canvasBitmap!!)
                bitmapCanvas!!.drawColor(erasePaint.color)

                canvasBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                bitmapCanvas2 = Canvas(canvasBitmap2!!)
                bitmapCanvas2!!.drawColor(erasePaint.color)

                holder.setFixedSize(width, height)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, " createBitmap")
        }
    }

    fun setAudioParams(sampleRate: Int, minBufferSize: Int) {
        try {
            synchronized(holder) {
                dataTimeNs = (1000 * 1000000L * minBufferSize) / sampleRate
                Logger.getLogger("B2020Logger").log(Level.WARNING, " dataTime: " + dataTimeNs)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, " setMinBufferSize")
        }
    }
}