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
    private var backPaint: Paint = Paint()

    private fun init() {
        paint.color = Color.WHITE
        paint.strokeWidth = 1F
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        backPaint.color = Color.GRAY
        backPaint.strokeWidth = 3F
        backPaint.style = Paint.Style.STROKE
        backPaint.isAntiAlias = true
    }

    fun setData(dataList: ArrayList<DoubleArray>) {
        this.dataList = dataList
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {
            val margin = 5F
            val valueDivisor = 50F
            val stepHeight = (height / dataList!!.size).toFloat()
            val yScaleFactor = stepHeight / valueDivisor
            val maxValue = 0.5F * height
            val minValue = 5F

            for (index in 0 until dataList!!.size) {
                val data = dataList!![index]
                val yCenter = (height * (dataList!!.size - index) / dataList!!.size).toFloat()
                val path = Path()
                path.moveTo(margin, yCenter)
                val xScaleFactor = (width - 2 * margin) / data.size.toFloat()

                for (i in 1 until data.size) {
                    val xCoord = margin + i.toFloat() * xScaleFactor
                    var scaledValue = min(maxValue, (data[i] * yScaleFactor).toFloat())
                    if(scaledValue < minValue) {
                        scaledValue = 0F
                    }
                    val yCoord = yCenter - scaledValue

                    path.lineTo(xCoord, yCoord)
                }

                //canvas!!.drawRect(0F, yCenter, width.toFloat(), yCenter + stepHeight, erasePaint)

                path.lineTo(width - margin, yCenter)
                canvas?.drawPath(path, backPaint)
                canvas?.drawPath(path, paint)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canvas?.clipOutPath(path)
                }
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "onDraw")
        }
    }
}