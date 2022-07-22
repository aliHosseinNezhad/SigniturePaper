package com.gamapp.signaturepaper

import android.view.MotionEvent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.gamapp.signaturepaper.extensions.v
import com.gamapp.signaturepaper.models.PointerEvent

const val TAG = "MotionEventsTAG"

fun MotionEvent.offset(index: Int): Offset {
    return Offset(getX(index), getY(index))
}

private fun SignaturePaperState.send(event: PointerEvent) {
    this.pointerChannel.send(event)
}

/**
 * A channel to notify listeners of pointer events
 * */
class PointerEventChannel {
    private val listeners = mutableListOf<(event: PointerEvent) -> Unit>();
    fun clear() {
        listeners.clear()
    }

    /**
     * to send the pointer event
     * */
    fun send(event: PointerEvent) {
        synchronized(listeners) {
            listeners.forEach {
                it.invoke(event)
            }
        }
    }

    /**
     * to receive pointer events
     * */
    fun receive(scope: (event: PointerEvent) -> Unit) {
        listeners += scope
    }
}

/**
 * a function to check whether movement has occurred or reached a specified size by particular point.
 * @param id: pointer id
 * @param event : touch events
 * @param stroke : strokeWidth of signature path.
 * @return offset after motion event if movement is qualified or exist for specified pointer, else returns null
 * */
private fun SignaturePaperState.returnMoveOffsetIfExist(
    id: Int,
    event: MotionEvent,
    stroke: Float
): Offset? {
    val index = event.findPointerIndex(id)
    if (index == -1) return null
    val new = event.offset(index)

    val offset = points[id] ?: return new

    return if ((offset v new).size > stroke * 1.1f) {
        points[id] = new
        new
    } else null
}

/**
 * To handle MotionEvents
 * */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.motionEvents(state: SignaturePaperState, stroke: Float): Modifier {
    return pointerInteropFilter { event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val offset = event.offset(index)
                state.points[id] = offset
                state.send(
                    PointerEvent.Down(
                        id = id,
                        offset = event.offset(index)
                    )
                )
                true
            }
            MotionEvent.ACTION_MOVE -> {
                (0..9).forEach { id ->
                    val offset = state.returnMoveOffsetIfExist(id, event, stroke)
                    if (offset != null) {
                        state.send(
                            PointerEvent.Move(
                                id = id,
                                offset = offset
                            )
                        )
                    }
                }
                true
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                val id = event.getPointerId(event.actionIndex)
                state.points[id] = null
                state.send(
                    PointerEvent.Up(
                        id = id
                    )
                )
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                state.points.clear()
                state.send(PointerEvent.Cancel)
                true
            }
            else -> false
        }
    }
}