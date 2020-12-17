package de.stefanlober.b2020.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import de.stefanlober.b2020.PriorityThreadFactory
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.min


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

    private val path: Path = Path()
    private var canvas: Canvas? = null
    var xMarginFraction: Float = 0.02F
    var yMarginFraction: Float = 0.32F
    private var dataList: ArrayList<DoubleArray>? = null
    private var paint: Paint = Paint()
    private var erasePaint: Paint = Paint()

    private var threadRunning = false

    private fun init() {
        paint.color = Color.WHITE
        paint.strokeWidth = 2.5F
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        erasePaint.color = Color.BLACK
        //erasePaint.strokeWidth = 1F
        erasePaint.style = Paint.Style.FILL
        erasePaint.isAntiAlias = true

        isFocusable = false
        setZOrderOnTop(true)

        holder.addCallback(this)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        threadRunning = true

        Executors.newSingleThreadExecutor(PriorityThreadFactory(Thread.MAX_PRIORITY)).execute {
            while (threadRunning) {
                val startTime = System.currentTimeMillis()
                draw()
                val endTime = System.currentTimeMillis()
                val deltaTime = endTime - startTime
                val frameTime = 10
                if (deltaTime < frameTime) {
                    try {
                        Thread.sleep(frameTime - deltaTime)
                    } catch (ex: InterruptedException) {
                        Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " onDraw", ex)
                    }
                }
            }
        }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        threadRunning = false
    }

    fun setData(dataList: ArrayList<DoubleArray>) {
        this.dataList = dataList
    }

    private fun draw() {
        try {
            if(dataList == null) {
                return
            }

            val valueDivisor = 50F

            val xMargin = width * xMarginFraction
            val yMargin = height * yMarginFraction

            val count = dataList!!.size
            val stepHeight = ((height - 2 * yMargin) / count)
            val yScaleFactor = stepHeight / valueDivisor
            val maxValue = 0.5F * (height - 2 * yMargin)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas = holder.lockHardwareCanvas()
            }
            canvas?.drawColor(Color.BLACK)

            for (index in dataList!!.size - 1 downTo 0) {
                path.reset()

                val data = dataList!![index]
                val yCenter = (height - 2 * yMargin) * (count - index) / count + yMargin
                path.moveTo(xMargin, yCenter)
                val xScaleFactor = (width - 2 * xMargin) / data.size.toFloat()

                for (i in 1 until data.size - 1) {
                    val xCoord = xMargin + i.toFloat() * xScaleFactor
                    val scaledValue = min(maxValue, (data[i] * yScaleFactor).toFloat())
                    val yCoord = yCenter - scaledValue
                    path.lineTo(xCoord, yCoord)
                }

                path.lineTo(width - xMargin, yCenter)
                canvas?.drawPath(path, erasePaint)
                canvas?.drawPath(path, paint)
            }

            holder.unlockCanvasAndPost(canvas)
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " draw")
        }
    }
}