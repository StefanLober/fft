package de.stefanlober.b2020

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ChartView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var _data: ShortArray? = null
    private var paint: Paint = Paint()

    fun setData(data: ShortArray) {
        _data = data
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {
            if (_data != null) {
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(2f);
                paint.setAntiAlias(true)
                //canvas?.drawLine(2F, 200F, 100F, 400F, paint)

                var lastX = -1F
                for (index in 0.._data!!.size-1) {
                    val x = (index.toFloat() * width.toFloat()) / _data!!.size.toFloat()
                    if(x > lastX) {
                        //val nextX = ((index + 1).toFloat() * width.toFloat()) / _data!!.size.toFloat()
                        val y = (_data!![index].toInt()) / 20F
                        val nextY = (_data!![index + 1].toInt()) / 20F
                        canvas?.drawLine(lastX, (height / 2 + y), x, (height / 2 + nextY), paint)

                        lastX = x
                    }
                }
            }
        } catch (ex: Exception) {

        }

    }
}