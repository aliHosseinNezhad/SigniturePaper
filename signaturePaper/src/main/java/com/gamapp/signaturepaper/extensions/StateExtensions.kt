package com.gamapp.signaturepaper.extensions

import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.Snapshot

val <T> State<T>.readValue: T
    get() = noRead {
        value
    }

inline fun <T> noRead(block: @DisallowComposableCalls () -> T): T {
    return Snapshot.withoutReadObservation(block)
}