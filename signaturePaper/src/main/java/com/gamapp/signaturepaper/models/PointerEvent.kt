package com.gamapp.signaturepaper.models

import androidx.compose.ui.geometry.Offset

interface Position {
    val offset: Offset
}

interface Id {
    val id: Int
}

sealed interface PointerEvent {
    data class Up(override val id: Int) : PointerEvent, Id
    data class Down(override val id: Int, override val offset: Offset) : PointerEvent, Position, Id
    data class Move(override val id: Int, override val offset: Offset) : PointerEvent, Position, Id
    object Cancel : PointerEvent
}