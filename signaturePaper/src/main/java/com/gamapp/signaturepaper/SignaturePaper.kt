package com.gamapp.signaturepaper

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


/**
 * A layout that draw touch events on canvas to capture user signature and returns signature as bitmap with [SignaturePaperState.getAsBitmap] function.
 * @param modifier : [SignaturePaper] use maximum constraints of layout width and height. so use fixed size on Modifier.
 * @param state : the state object to be used to control SignaturePaper
 * @param colors : the colors used to change background color and signature color
 * @param maxStrokeWidth : used to set maximum strokeWidth of signature. stroke width of signature path decrease by rising in touch movement speed.
 **/
@Composable
fun SignaturePaper(
    modifier: Modifier = Modifier,
    state: SignaturePaperState = rememberSignaturePaperState(),
    colors: SignaturePaperColors = SignatureDefaults.colors(),
    maxStrokeWidth: Float = 10f
) {
    Snapshot.withoutReadObservation {
        state.colors = colors
        state.strokeWidth = maxStrokeWidth
    }
    LaunchedEffect(key1 = state) {
        val size = derivedStateOf { state.width to state.height }
        val sizeFlow = snapshotFlow {
            size.value
        }
        val colorFlow = snapshotFlow { state.colors }
        val bitmapFlow = snapshotFlow { state.signatureBitmap }
        launch {
            bitmapFlow.collectLatest { bitmap ->
                state.signatureCanvas = if (bitmap != null) {
                    Canvas(bitmap)
                } else null
            }
        }
        //to create new bitmap as soon as layout width and height is changed.
        launch {
            sizeFlow.collectLatest {
                val w = it.first
                val h = it.second
                state.createSignatureBitmap(w, h)
            }
        }
        launch {
            colorFlow.combine(sizeFlow, transform = { a, b ->
                a to b
            }).collectLatest {
                val c = it.first
                val w = it.second.first
                val h = it.second.second
                state.backgroundBitmap = if (w != null && h != null) {
                    ImageBitmap(w, h).apply {
                        if (c != null) {
                            val canvas = Canvas(this)
                            canvas.nativeCanvas.drawColor(c.backgroundColor.toArgb())
                        }
                    }
                } else null
            }
        }
    }
    // receives pointer events and calls SignaturePaper.onPointerEvents
    DisposableEffect(key1 = state) {
        state.pointerChannel.receive {
            state.onPointerEvents(it)
        }
        onDispose {
            state.pointerChannel.clear()
            state.reset()
        }
    }
    BoxWithConstraints(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        Snapshot.withoutReadObservation {
            state.width = constraints.maxWidth
            state.height = constraints.maxHeight
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .motionEvents(state, stroke = maxStrokeWidth)
                .drawBehind {
                    val rect = size.toRect()
                    val r = rect.toAndroidRect()
                    val rectF = rect.toAndroidRectF()
                    val b = state.backgroundBitmap
                    if (b != null) {
                        drawContext.canvas.nativeCanvas.drawBitmap(
                            b.asAndroidBitmap(),
                            r,
                            rectF,
                            null
                        )
                    }
                }
                .drawBehind {
                    state.refresh.let {
                        val rect = size.toRect()
                        val r = rect.toAndroidRect()
                        val rectF = rect.toAndroidRectF()
                        val b = state.signatureBitmap
                        if (b != null) {
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                b.asAndroidBitmap(),
                                r,
                                rectF,
                                null
                            )
                        }
                    }
                }
        )
    }
}