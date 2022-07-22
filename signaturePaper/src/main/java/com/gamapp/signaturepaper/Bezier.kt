package com.gamapp.signaturepaper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import com.gamapp.signaturepaper.extensions.v
import com.gamapp.signaturepaper.models.Element
import java.lang.Exception


/**
 * this function make bezier curve by [p1],[p2],[p3] .
 * @param [value] must be between 0 .. 1
 * @return [Offset] for [value]
 * */
fun quadraticBezierFunction(p1: Offset, p2: Offset, p3: Offset, value: Float): Offset {
    return try {
        ((p1 * value * value) + (p2 * value * (1 - value) * 2f) + p3 * (1 - value) * (1 - value))
    } catch (e: Exception) {
        p3
    }
}

/**
 * draw a curve by three points on canvas
 * @param a point 1
 * @param b point 2
 * @param c point 3
 * @param canvas
 * @param paint
 * @param strokeWidth of the curve
 * */
fun drawBezierCurve(
    a: Element,
    b: Element,
    c: Element,
    canvas: Canvas,
    paint: Paint,
    strokeWidth: Float
) {
    // strokeWeight delta
    val dsw = a.strokeWeight - c.strokeWeight
    //start strokeWeight
    val ssw = c.strokeWeight
    // here we calculate size of two side of these three points and sum them. (b is middle point)
    val size = (b.offset v c.offset).size + (a.offset v b.offset).size
    for (j in 0..size.toInt()) {
        //here we draw circle repeatedly to draw curve without gap
        val offset = quadraticBezierFunction(a.offset, b.offset, c.offset, (j / size))
        //weight is variable in curve
        val strokeWeight = (ssw + (dsw / size) * (j))
        canvas.drawCircle(
            center = offset,
            radius = strokeWidth * strokeWeight,
            paint = paint
        )
    }
}



fun drawLine(canvas: Canvas, p1: Element, p2: Element, paint: Paint, strokeWidth: Float) {
    // strokeWeight delta
    val dsw = p2.strokeWeight - p1.strokeWeight
    //start strokeWeight
    val ssw = p1.strokeWeight

    val size = (p1.offset v p2.offset).size
    val df = p2.offset - p1.offset
    for (j in 0..size.toInt()) {
        //here we draw circle repeatedly to draw curve without gap
        val offset = df * (j / size) + p1.offset
        //weight is variable in curve
        val strokeWeight = (ssw + (dsw / size) * (j))
        canvas.drawCircle(
            center = offset,
            radius = strokeWidth * strokeWeight,
            paint = paint
        )
    }
}



