package com.gamapp.signaturepaper.extensions

import androidx.compose.ui.geometry.Offset
import com.gamapp.signaturepaper.models.Vector

infix fun Offset.v(offset: Offset): Vector {
    return Vector(
        dx = this.x - offset.x,
        dy = this.y - offset.y
    )
}




