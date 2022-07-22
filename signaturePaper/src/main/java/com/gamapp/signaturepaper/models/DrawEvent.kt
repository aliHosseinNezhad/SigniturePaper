package com.gamapp.signaturepaper.models

sealed class DrawEvent {

    data class Draw(
        val point: Point
    ) : DrawEvent()

    object Clear : DrawEvent()

}