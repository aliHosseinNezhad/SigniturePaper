package com.gamapp.signaturepaper

import androidx.compose.ui.geometry.Offset
import com.gamapp.signaturepaper.abstracts.PointerEventsState
import com.gamapp.signaturepaper.models.*

internal class PointerEventsStateImpl(private val state: SignaturePaperState) : PointerEventsState {
    override val points: MutableMap<Int, Offset?> = mutableMapOf()

    override fun onEvent(event: PointerEvent) {
        val check = if (event is Id) {
            event.id == 0
        } else event is PointerEvent.Cancel
        if (check) {
            when (event) {
                is PointerEvent.Down -> {
                    val point = Point(
                        offset = event.offset,
                        previous = null,
                        next = null
                    )
                    val path = Object.Path(
                        point = point,
                        color = state.colors?.signatureColor ?: return,
                        strokeWidth = state.strokeWidth
                    )
                    state.paper.addLayer(
                        DrawLayer(path)
                    )
                    state.draw(path)
                }
                is PointerEvent.Move -> {
                    val lastLayer = state.paper.currentLayer() ?: return
                    val color = lastLayer.path.color
                    val strokeWidth = lastLayer.path.strokeWidth
                    val prev = lastLayer.path.point
                    val point = Point(
                        offset = event.offset,
                        previous = prev,
                        next = null,
                    ).apply {
                        prev.next = this
                    }
                    val path = Object.Path(
                        point = point,
                        color = color,
                        strokeWidth = strokeWidth
                    )
                    lastLayer.path = path
                    state.draw(path)
                }
                is PointerEvent.Up,
                is PointerEvent.Cancel -> {
                }
            }
        }
    }
}