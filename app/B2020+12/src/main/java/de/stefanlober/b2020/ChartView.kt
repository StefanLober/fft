package de.stefanlober.b2020

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

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

    private var _data: ShortArray? = null
    private var paint: Paint = Paint()

    private fun init() {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true)
    }

    fun setData(data: ShortArray) {
        _data = data
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {
            if (_data != null) {
                var lastX = -1F
                var lastY = -1F
                for (index in 0 until _data!!.size) {
                    val x = (index.toFloat() * width.toFloat()) / _data!!.size.toFloat()
                    if(x > lastX + 1) {
                        val y = (_data!![index].toInt()) / 20F
                        canvas?.drawLine(lastX, (height / 2 + lastY), x, (height / 2 + y), paint)

                        lastX = x
                        lastY = y
                    }
                }
            }
        } catch (ex: Exception) {
        }
    }
}