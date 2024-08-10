package com.example.trippie.Utils
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class LinedEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    private val linePaint: Paint = Paint().apply {
        color = context.getColor(android.R.color.black) // Set line color to black
        strokeWidth = 2f // Customize your line width
    }

    private val rect: Rect = Rect()

    override fun onDraw(canvas: Canvas) {
        val count = lineCount
        val lineHeight = lineHeight
        var baseline = getLineBounds(0, rect)

        for (i in 0 until count) {
            canvas.drawLine(
                rect.left.toFloat(),
                (baseline + 1).toFloat(),
                rect.right.toFloat(),
                (baseline + 1).toFloat(),
                linePaint
            )
            baseline += lineHeight
        }

        super.onDraw(canvas)
    }
}
