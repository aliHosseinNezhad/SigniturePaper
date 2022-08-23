package com.gamapp.signaturepaper

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import com.gamapp.signaturepaper.extensions.v
import com.gamapp.signaturepaper.models.*
import com.gamapp.signaturepaper.models.Object.Path

fun drawPath(path: Path, canvas: Canvas) {
    var point: Point? = path.point.first()
    while (point != null) {
        drawPathSegment(point, canvas = canvas, paint = path.paint, strokeWidth = path.strokeWidth)
        point = point.next
    }
}


fun drawPathSegment(point: Point, paint: Paint, canvas: Canvas, strokeWidth: Float) {
    val prev = point.previous
    if (prev != null) {
        val p3 = point middle prev
        val p2 = Element(prev.offset, prev.strokeWeight)
        val prevPrev = prev.previous
        if (prevPrev != null) {
            val p1 = (prev middle prevPrev)
            drawBezierCurve(p1, p2, p3, canvas, paint, strokeWidth)
        } else {
            drawLine(canvas, p2, p3, paint, strokeWidth = strokeWidth)
        }
    } else {
        canvas.drawCircle(
            center = point.offset,
            radius = strokeWidth * point.strokeWeight,
            paint = paint
        )
    }
}

fun roundPath(path: Path) {
    (1..5).forEach {
        roundPath(path, minSize = it * 5f)
    }
}

fun roundPath(path: Path, minSize: Float = path.strokeWidth * 5f) {
    var item: Point? = path.point
    do {
        val last = item ?: break
        val prev = item.previous ?: break
        val v = last.offset v prev.offset
        val size = v.size
        if (minSize >= size) {
            val t = prev.previous
            if (t == null) {
                item = prev
                continue
            }
            val s = (t.offset v last.offset).size
            if (s <= minSize) {
                t.next = last
                last.previous = t
            } else {
                item = prev
            }
        } else {
            item = prev
        }
    } while (true)
}