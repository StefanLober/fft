package de.stefanlober.b2020.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.logging.Level
import java.util.logging.Logger

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

    private fun init() {
        paint.color = Color.WHITE
        paint.strokeWidth = 3F
        paint.style = Paint.Style.STROKE
        //paint.isAntiAlias = true

        erasePaint.color = Color.BLACK
        erasePaint.style = Paint.Style.FILL
        erasePaint.isAntiAlias = true
    }

    fun setData(dataList: ArrayList<DoubleArray>) {
        this.dataList = dataList
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {
            val valueDivisor = 1000F
            val stepHeight = (height / dataList!!.size).toFloat()
            val scaleFactor = stepHeight / valueDivisor
            val stepWidth = 2F

            for (index in dataList!!.size-1 downTo 0) {
                val data = dataList!![index]
                var lastXCoord = -1F
                val yCenter = (height * (dataList!!.size - index) / dataList!!.size).toFloat()
                val path = Path()
                path.moveTo(lastXCoord, yCenter)

                for (i in 1 until data.size) {
                    val xCoord = (i.toFloat() * width.toFloat()) / data.size.toFloat()
                    if (xCoord >= lastXCoord + stepWidth) {
                        val scaledValue = (data[i] * scaleFactor).toFloat()
                        val yCoord = yCenter - scaledValue

                        path.lineTo(xCoord, yCoord)

                        lastXCoord = xCoord
                    }
                }

                //canvas!!.drawRect(0F, yCenter, width.toFloat(), yCenter + stepHeight, erasePaint)

                path.lineTo(lastXCoord, yCenter)
                canvas?.drawPath(path, erasePaint)
                canvas?.drawPath(path, paint)
            }
        } catch (ex: Exception) {
            Logger.getLogger("B2020Logger").log(Level.WARNING, "onDraw")
        }
    }
}