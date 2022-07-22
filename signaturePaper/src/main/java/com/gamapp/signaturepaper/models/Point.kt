package com.gamapp.signaturepaper.models

import androidx.compose.ui.geometry.Offset
import com.gamapp.signaturepaper.extensions.v
import kotlin.math.exp

/**
 * to calculate Stroke Weight of a point by
 * */
private fun calculateStrokeWeight(input: Float): Float {
    val v = input / 100f
    return (exp(v * v * -1f)) * 0.8f + 0.2f
}

/**
 * A point that have a reference to previous point in the path
 * @param offset the point offset
 * @param previous reference to previous point in the path. "previous" is null if point is first point of the path
 * */
data class Point(val offset: Offset, val previous: Point?) {
    val strokeWeight by lazy {
        calculateStrokeWeight(distance)
    }

    private val distance by lazy {
        val offset = previous?.offset
        if (offset == null) {
            0f
        } else {
            (offset v this.offset).size
        }
    }
}


infix fun Point.middle(point: Point): Element {
    val offset = (this.offset + point.offset) / 2f
    val weight = (this.strokeWeight + point.strokeWeight) / 2f
    return Element(
        offset = offset,
        strokeWeight = weight
    )
}
