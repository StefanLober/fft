package de.stefanlober.b2020.view

import android.content.Context
import android.graphics.*
import android.os.Build
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
        Logger.getLogger("B2020Logger").log(Level.INFO,  System.currentTimeMillis().toString() + " onDraw " + Thread.currentThread().name)

        super.onDraw(canvas)

        val start = System.currentTimeMillis()

        try {
            val margin = 50F
            val valueDivisor = 50F
            val stepHeight = (height / dataList!!.size).toFloat()
            val yScaleFactor = stepHeight / valueDivisor
            val maxValue = 0.5F * height

            for (index in dataList!!.size - 1 downTo 0) {
                path.reset()

                val data = dataList!![index]
                val yCenter = (height - margin) * (dataList!!.size - index) / dataList!!.size + margin
                path.moveTo(margin, yCenter)
                val xScaleFactor = (width - 2 * margin) / data.size.toFloat()

                for (i in 1 until data.size) {
                    val xCoord = margin + i.toFloat() * xScaleFactor
                    var scaledValue = min(maxValue, (data[i] * yScaleFactor).toFloat())
                    val yCoord = yCenter - scaledValue

                    path.lineTo(xCoord, yCoord)
                }

                path.lineTo(width - margin, yCenter)
                canvas?.drawPath(path, erasePaint)
                canvas?.drawPath(path, paint)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, System.currentTimeMillis().toString() + " onDraw")
        }

        val end = System.currentTimeMillis()
        Logger.getLogger("B2020Logger").log(Level.INFO, "draw: " + (end - start).toString())
    }
}