package com.gamapp.signaturepaper.abstracts

import androidx.compose.ui.geometry.Offset
import com.gamapp.signaturepaper.models.PointerEvent

interface PointerEventsState {
    /**
     * To store last position of each pointer.
     * */
    val points:MutableMap<Int,Offset?>

    /**
     * This function is called on each pointer events and make convenient data to draw them.
     * */
    fun onEvent(event:PointerEvent)
}