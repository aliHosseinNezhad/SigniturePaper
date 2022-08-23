package com.gamapp.signiturepaper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.gamapp.signaturepaper.*
import com.gamapp.signaturepaper.abstracts.PointerEventsState
import com.gamapp.signaturepaper.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


object PermissionManager {
    fun requestWritePermission(
        context: Context,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
    ) {
        if (!hasPermission(context)) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,

                    )
            )
        }
    }

    fun hasPermission(context: Context): Boolean {
        var check = ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        Log.i(TAG, "hasPermission: read permission $check")
        check = check && (ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        Log.i(TAG, "hasPermission: write permission $check")
        return check
    }
}

object ImageSavingManager {
    private fun saveImageIntent(title: String, uri: Uri? = null): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_TITLE, title)
            Intent.EXTRA_ALLOW_MULTIPLE
            Intent.EXTRA_TITLE

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            if (uri != null && Build.VERSION.SDK_INT >= 26)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        }
    }

    private val activityResult = Channel<ActivityResult>(capacity = Channel.UNLIMITED)
    private var launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    @Composable
    fun RememberLauncher() {
        launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                activityResult.trySend(it)
            })
    }

    suspend fun save(
        context: Context,
        title: String = "Signature.png",
        bitmap: Bitmap,
        message: (String) -> Unit
    ) {
        val contentResolver = context.contentResolver
        val launcher = this.launcher
        if (launcher != null) {
            launcher.launch(saveImageIntent(title))
            activityResult.receiveAsFlow().take(1).collectLatest {
                if (it.resultCode == Activity.RESULT_OK) {
                    try {
                        it.data?.data?.let { uri ->
                            contentResolver.openFileDescriptor(uri, "r")?.let {

                            }
                            var success: Boolean
                            withContext(context = Dispatchers.IO) {
                                success = bitmap.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    contentResolver.openOutputStream(uri)
                                )
                            }
                            if (success)
                                message("image is successfully saved")
                            else message("image is not saved!")
                        } ?: kotlin.run {
                            message("image is not saved!")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        message("image is not saved!")
                    }
                } else message("image is not saved!")
            }
        }
    }
}


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            ComplexSample()
        }
    }
}
