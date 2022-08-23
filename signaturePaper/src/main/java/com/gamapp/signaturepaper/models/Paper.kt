package com.gamapp.signaturepaper.models

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Skippable {
    val canSkipToNext: Boolean
    val canSkipToPrevious: Boolean
    fun skipToNext(): Boolean
    fun skipToPrevious(): Boolean
}


internal class Paper internal constructor() : Skippable {
    private val skipChannel = Channel<Int>(capacity = Channel.UNLIMITED)
    internal val skipFlow: Flow<Int> = skipChannel.receiveAsFlow()

    private var currentLayer: DrawLayer? by mutableStateOf(null)
    private var rootLayer: DrawLayer? by mutableStateOf(null)

    override val canSkipToPrevious by derivedStateOf {
        currentLayer != null
    }

    override val canSkipToNext by derivedStateOf {
        val current = currentLayer
        if (current == null && rootLayer != null) true else current?.next != null
    }

    fun addLayer(drawLayer: DrawLayer) {
        if (currentLayer == null) {
            rootLayer = drawLayer
        }
        val prev = currentLayer
        currentLayer = drawLayer
        prev?.next = drawLayer
        drawLayer.previous = prev
    }

    fun currentLayer(): DrawLayer? {
        return currentLayer
    }

    fun getCurrentLayerOrPut(default: () -> DrawLayer): DrawLayer {
        val current = currentLayer
        return current ?: default().apply {
            addLayer(this)
        }
    }

    override fun skipToNext(): Boolean {
        if (canSkipToNext) {
            currentLayer = if (currentLayer == null) {
                rootLayer
            } else
                currentLayer?.next ?: return false
            skipChannel.trySend(1)
            return true
        }
        return false
    }

    override fun skipToPrevious(): Boolean {
        if (canSkipToPrevious) {
            currentLayer = currentLayer?.previous
            skipChannel.trySend(-1)
            return true
        }
        return false
    }

    fun clear() {
        rootLayer = null
        currentLayer = null
    }
}