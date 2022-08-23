package com.gamapp.signaturepaper.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class DrawLayer(
    internal var path: Object.Path
) : LinkedObject<DrawLayer> {
    override var previous: DrawLayer? by mutableStateOf(null)
    override var next: DrawLayer? by mutableStateOf(null)
}