package com.gamapp.signaturepaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.core.graphics.toRectF
import com.gamapp.signaturepaper.models.*


@Composable
fun rememberSignaturePaperState() = remember {
    SignaturePaperState()
}

/**
 * A state object that can be hoisted to control and access [SignaturePaper].
 * */
class SignaturePaperState {
    internal var strokeWidth: Float by mutableStateOf(8f)
    internal var colors by mutableStateOf<SignaturePaperColors?>(null)
    internal var width by mutableStateOf<Int?>(null)
    internal var height by mutableStateOf<Int?>(null)
    internal var refresh by mutableStateOf(0)

    internal var backgroundBitmap: ImageBitmap? by mutableStateOf(null)

    internal var signatureBitmap by mutableStateOf<ImageBitmap?>(null)
    internal var signatureCanvas: Canvas? by mutableStateOf(null)

    private val paint by derivedStateOf {
        val color = colors?.signatureColor ?: Color.Blue
        Paint().apply {
            isAntiAlias = true
            this.color = color
            style = PaintingStyle.Fill
        }
    }

    @PublishedApi
    internal val points = mutableStateMapOf<Int, Offset?>()

    private fun refresh() {
        Snapshot.withoutReadObservation {
            refresh += (refresh + 1) % 100
        }
    }

    internal fun createSignatureBitmap(w: Int? = width, h: Int? = height): ImageBitmap? {
        this.signatureBitmap = if (w != null && h != null) {
            if (w > 0 && h > 0)
                ImageBitmap(w, h)
            else null
        } else null
        return this.signatureBitmap
    }

    private var currentPoint: Point? = null

    internal val pointerChannel = PointerEventChannel()

    /**
     * @return signature as a bitmap
     * */
    fun getAsBitmap(): Bitmap? {
        val signatureBitmap = signatureBitmap?.asAndroidBitmap() ?: return null
        val backgroundBitmap = backgroundBitmap?.asAndroidBitmap() ?: return null
        val w = width ?: return null
        val h = height ?: return null
        val bitmap = ImageBitmap(w, h)
        val canvas = Canvas(bitmap)
        val rect = canvas.nativeCanvas.clipBounds
        val rectF = rect.toRectF()
        canvas.nativeCanvas.drawBitmap(backgroundBitmap, rect, rectF, null)
        canvas.nativeCanvas.drawBitmap(signatureBitmap, rect, rectF, null)
        return bitmap.asAndroidBitmap()
    }

    /**
     * to clear every thing is drawn.
     * */
    fun clear() {
        currentPoint = null
        createSignatureBitmap()
    }

    /**
     * this function is called on each pointer events and make convenient data to draw them.
     * */
    internal fun onPointerEvents(it: PointerEvent) {
        val check = if (it is Id) {
            it.id == 0
        } else it is PointerEvent.Cancel
        if (check) {
            when (it) {
                is PointerEvent.Move,
                is PointerEvent.Down -> {
                    it as Position
                    val prev = currentPoint
                    val current = Point(offset = it.offset, previous = prev)
                    currentPoint = current
                    draw(
                        DrawEvent.Draw(point = current)
                    )
                }
                is PointerEvent.Up,
                is PointerEvent.Cancel -> {
                    currentPoint = null
                }
            }
        }
    }

    /**
     * to draw on signatureBitmap
     * */
    private fun draw(it: DrawEvent) {
        val canvas = this.signatureCanvas
        if (it is DrawEvent.Draw && canvas != null) {
            paint(point = it.point, canvas = canvas, paint, strokeWidth = strokeWidth)
        } else if (it is DrawEvent.Clear) {
            clear()
        }
        refresh()
    }

    /**
     * this function is used only when state is no longer used.
     * */
    internal fun reset() {
        clear()
        width = null
        height = null
        colors = null
        signatureBitmap = null
        backgroundBitmap = null
        signatureCanvas = null
    }
}


fun paint(point: Point, canvas: Canvas, paint: Paint, strokeWidth: Float = 10f) {
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


