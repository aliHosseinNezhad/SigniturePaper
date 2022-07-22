package com.gamapp.signaturepaper.extensions

import android.graphics.Bitmap
import android.util.Base64
import com.gamapp.signaturepaper.SignaturePaperState
import java.io.ByteArrayOutputStream

/**
 * @return signature as a byte array
 * */
fun SignaturePaperState.getAsByteArray(): ByteArray? {
    val bmp = getAsBitmap()?:return null
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray: ByteArray = stream.toByteArray()
    bmp.recycle()
    return byteArray
}


/**
 * @return signature as a string encoded by base64 encoder
 * */
fun SignaturePaperState.getAsBase64():String? {
    val byteArray = getAsByteArray()?:return null
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}