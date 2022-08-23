package com.gamapp.signaturepaper

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.*
import androidx.core.graphics.toRectF
import com.gamapp.signaturepaper.models.*
import kotlinx.coroutines.flow.collectLatest


@Composable
fun rememberSignaturePaperState() = remember {
    SignaturePaperState()
}

/**
 * A state object that can be hoisted to control and access [SignaturePaper].
 * */
class SignaturePaperState internal constructor(
    internal val paper: Paper
) : Skippable by paper {
    constructor() : this(paper = Paper())

    internal var strokeWidth: Float by mutableStateOf(8f)
    internal var colors by mutableStateOf<SignaturePaperColors?>(null)
    internal var width by mutableStateOf<Int?>(null)
    internal var height by mutableStateOf<Int?>(null)
    internal var refresh by mutableStateOf(0)

    internal var backgroundBitmap: ImageBitmap? by mutableStateOf(null)

    internal var signatureBitmap by mutableStateOf<ImageBitmap?>(null)
    internal var signatureCanvas: Canvas? by mutableStateOf(null)

    internal val pointerEventState = PointerEventsStateImpl(this)


    private fun refresh() {
        Snapshot.withoutReadObservation {
            refresh += (refresh + 1) % 100
        }
    }

    internal fun createSignatureBitmap(w: Int? = width, h: Int? = height): ImageBitmap? {
        return if (w != null && h != null) {
            if (w > 0 && h > 0)
                ImageBitmap(w, h)
            else null
        } else null
    }

    internal fun createSignatureBitmapAndSet(w: Int? = width, h: Int? = height): ImageBitmap? {
        this.signatureBitmap = createSignatureBitmap(w, h)
        return this.signatureBitmap
    }

    fun roundSignature() {
        val bitmap = createSignatureBitmap() ?: return
        val canvas = Canvas(bitmap)
        val current = paper.currentLayer() ?: return
        current.first().let { it ->
            it.iterateOnNextUntil {
                roundPath(it.path)
                drawPath(it.path, canvas)
                current != it
            }
            signatureBitmap = bitmap
        }
    }


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
     * To clear every thing is drawn.
     * */
    fun clear() {
        paper.clear()
        createSignatureBitmapAndSet()
    }

    /**
     * To draw on signatureBitmap
     * */
    internal fun draw(it: Object) {
        val canvas = this.signatureCanvas
        if (it is Object.Path && canvas != null) {
            drawPathSegment(
                point = it.point,
                canvas = canvas,
                paint = it.paint,
                strokeWidth = it.strokeWidth
            )
        }
        refresh()
    }

    /**
     * This function is used only when state is no longer used.
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

    internal suspend fun onSkip() {
        paper.skipFlow.collectLatest {
            //skip to next
            if (it == 1) {
                val layer = paper.currentLayer() ?: return@collectLatest
                onSkipToNext(canvas = signatureCanvas ?: return@collectLatest, layer)
            }
            //skip to previous
            if (it == -1) {
                val layer = paper.currentLayer()
                val bitmap = createSignatureBitmap()
                val canvas = bitmap?.let { it1 -> Canvas(it1) }
                if (canvas != null && layer != null) {
                    onSkipToPrevious(canvas, layer)
                }
                signatureBitmap = bitmap
            }
        }
    }
}

fun onSkipToNext(canvas: Canvas, layer: DrawLayer) {
    drawPath(layer.path, canvas)
}

fun onSkipToPrevious(canvas: Canvas, layer: DrawLayer) {
    val first = layer.first()
    first.iterateOnNextUntil {
        drawPath(path = it.path, canvas = canvas)
        it != layer
    }
}
