package com.app.design_motion.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class DrawableLeftTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    override fun onDraw(canvas: Canvas?) {
        val drawableLeft = compoundDrawables[0]
        if (drawableLeft != null) {
            val textWidth = paint.measureText(text.toString())
            val drawablePadding = compoundDrawablePadding
            val drawableWidth = drawableLeft.intrinsicWidth
            val bodyWidth = textWidth + drawableWidth + drawablePadding
            canvas?.translate((width - bodyWidth) / 2, 0f)
        }
        super.onDraw(canvas)
    }
}