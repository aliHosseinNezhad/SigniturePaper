package com.gamapp.signaturepaper.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle

sealed class Object {

    data class Path(
        val point: Point,
        val color: Color,
        val strokeWidth: Float,
    ) : Object() {
        val paint by lazy {
            Paint().apply {
                this.isAntiAlias = true
                this.color = this@Path.color
                this.style = PaintingStyle.Fill
            }
        }
    }

}