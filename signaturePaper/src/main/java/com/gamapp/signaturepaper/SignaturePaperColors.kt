package com.gamapp.signaturepaper

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Represents the colors used by a [SignaturePaper].
 * See [SignatureDefaults.colors] for the default implementation.
 * */
class SignaturePaperColors(
    /**
     * background color of [SignaturePaper]
     * */
    val backgroundColor: Color,
    /**
     * signature color of [SignaturePaper]
     * */
    val signatureColor: Color
)

/**
 * object to hold defaults by [SignaturePaper]
 * */
object SignatureDefaults {

    /**
     * Creates a [SignaturePaperColors] that represents the different colors used in parts of the [SignaturePaper].
     * */
    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colors.surface,
        signatureColor: Color = MaterialTheme.colors.onSurface
    ): SignaturePaperColors {
        val colors = remember(backgroundColor) {
            SignaturePaperColors(
                backgroundColor = backgroundColor,
                signatureColor = signatureColor,
            )
        }
        return colors
    }
}