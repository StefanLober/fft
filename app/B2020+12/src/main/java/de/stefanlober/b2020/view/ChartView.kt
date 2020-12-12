package de.stefanlober.b2020.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.min

class ChartView : View {
    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    var xMarginFraction: Float = 0.02F
    var yMarginFraction: Float = 0.32F
    private var dataList: ArrayList<DoubleArray>? = null
    private var paint: Paint = Paint()
    private var erasePaint: Paint = Paint()
    private val path = Path()

    private fun init() {
        paint.color = Color.WHITE
        paint.strokeWidth = 2.5F
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        erasePaint.color = Color.BLACK
        //erasePaint.strokeWidth = 1F
        erasePaint.style = Paint.Style.FILL
        erasePaint.isAntiAlias = true
    }

    fun setData(dataList: ArrayList<DoubleArray>) {
        this.dataList = dataList
    }

    override fun onDraw(canvas: Canvas?) {
        Logger.getLogger("B2020Logger").log(Level.INFO,  (System.currentTimeMillis() % 3600000).toString() + " onDraw")

        super.onDraw(canvas)

        //val start = System.currentTimeMillis()

        try {
            val valueDivisor = 50F

            val xMargin = width * xMarginFraction
            val yMargin = height * yMarginFraction

            val count = dataList!!.size
            val stepHeight = ((height - 2 * yMargin) / count).toFloat()
            val yScaleFactor = stepHeight / valueDivisor
            val maxValue = 0.5F * (height - 2 * yMargin)

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
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, (System.currentTimeMillis() % 3600000).toString() + " onDraw")
        }

        //val end = System.currentTimeMillis()
        //Logger.getLogger("B2020Logger").log(Level.INFO, "draw: " + (end - start).toString())
    }

    private fun drawGraph(canvas: Canvas?, frameOffset: Int) {
    }
}