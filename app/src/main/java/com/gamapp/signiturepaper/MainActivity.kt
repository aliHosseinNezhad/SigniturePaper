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
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gamapp.signaturepaper.*
import com.gamapp.signiturepaper.ui.theme.SigniturePaperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

object FileManager {
    private val files = mutableStateListOf<List<File>>(listOf())
    val currentFiles = derivedStateOf {
        files.last()
    }

    fun init() {
        val file = Environment.getExternalStorageDirectory()
        setFileListByFile(file)
    }

    fun back() {
        if (files.size > 2)
            files.removeLastOrNull()
    }

    fun setFileListByFile(file: File) {
        files += file.listFiles()?.mapNotNull { it } ?: emptyList()
    }

    fun write() {
        val file = Environment.getExternalStorageDirectory()
        val new = File(file, "SignaturePaperFolder")
        if (!new.exists()) {
            new.mkdir()
        }
    }


}

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
                            var success:Boolean
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
            SigniturePaperTheme {
                PermissionScreen {
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    val state = rememberSignaturePaperState()
                    var value by remember {
                        mutableStateOf(0f)
                    }
                    var stroke by remember {
                        mutableStateOf(1.dp)
                    }
                    val palette = palette()
                    var signatureColors by remember {
                        mutableStateOf(
                            SignaturePaperColors(
                                backgroundColor = Color.Transparent,
                                Color(0, 100, 255)
                            )
                        )
                    }
                    ImageSavingManager.RememberLauncher()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray.copy(0.5f))
                    ) {
                        Slider(value = value, onValueChange = {
                            value = it
                        })
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Points {
                                stroke = it
                            }
                            SignaturePaper(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.Black)
                                    .wrapContentSize(),
                                state = state,
                                maxStrokeWidth = stroke,
                                colors = signatureColors,
                            )
                            Palette(
                                modifier = Modifier
                                    .width(40.dp + 50.dp * (value))
                                    .fillMaxHeight(),
                                colors = palette,
                                onSelect = {
                                    signatureColors = it
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Button(
                                onClick = state::clear,
                                modifier = Modifier
                                    .wrapContentHeight()
                            ) {
                                Text(text = "Clear")
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        ImageSavingManager.save(
                                            context,
                                            bitmap = state.getAsBitmap() ?: return@launch
                                        ) {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                            ) {
                                Text(text = "Save signature")
                            }

                        }

                    }

                }
            }
        }
    }
}

@Composable
fun PermissionScreen(screen: @Composable () -> Unit) {
    val context = LocalContext.current
    val permission = remember {
        mutableStateOf(PermissionManager.hasPermission(context))
    }
    if (permission.value) {
        screen()
    } else {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = {
                permission.value = PermissionManager.hasPermission(context)
            }
        )
        LaunchedEffect(key1 = Unit) {
            PermissionManager.requestWritePermission(context, launcher)
        }


    }
}


@Composable
fun palette(): List<SignaturePaperColors> {
    val colors = remember {
        listOf(
            SignaturePaperColors(
                backgroundColor = Color.Transparent,
                signatureColor = Color.Blue
            ),
            SignaturePaperColors(
                backgroundColor = Color.White,
                signatureColor = Color.Blue
            ),
            SignaturePaperColors(
                backgroundColor = Color.White,
                signatureColor = Color.Red
            ),
            SignaturePaperColors(
                backgroundColor = Color.White,
                signatureColor = Color.Black
            ),
            SignaturePaperColors(
                backgroundColor = Color.Blue,
                signatureColor = Color.White
            ),
            SignaturePaperColors(
                backgroundColor = Color.Black,
                signatureColor = Color.Yellow
            ),
            SignaturePaperColors(
                backgroundColor = Color.Blue,
                signatureColor = Color.Yellow
            ),
            SignaturePaperColors(
                backgroundColor = Color.Black,
                signatureColor = Color.White
            ),
        )
    }
    return colors
}

@Composable
fun Palette(
    modifier: Modifier,
    colors: List<SignaturePaperColors>,
    onSelect: (SignaturePaperColors) -> Unit
) {
    LazyColumn(
        modifier = modifier.clip(shape = RectangleShape),
        verticalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        items(colors) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(1.dp, Color.Blue, shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        onSelect(it)
                    }) {
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(it.backgroundColor)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(it.signatureColor)
                )
            }
        }
    }
}

@Composable
fun Points(onSet: (Dp) -> Unit) {
    var selected by remember {
        mutableStateOf<Dp?>(null)
    }
    val points = remember {
        listOf(
            0.5f.dp,
            1.dp,
            2.dp,
            3.dp,
            5.dp,
            6.dp,
            8.dp,
            10.dp,
            20.dp
        )
    }
    Column(
        modifier = Modifier
            .width(50.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        for (point in points) {
            val color by remember(point) {
                derivedStateOf {
                    if(selected == point) Color.Blue.copy(0.3f)
                    else Color.Transparent
                }
            }
            Point(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .clickable {
                        selected = point
                        onSet(point)
                    }
                    .padding(vertical = 4.dp)
                    .width(50.dp)
                    .heightIn(50.dp)
                    , stroke = point
            )
        }
    }
}

@Composable
fun Point(modifier: Modifier, stroke: Dp) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(stroke * 2)
        ) {
            drawCircle(Color.Black, radius = stroke.toPx())
        }
        Text(
            text = "${stroke.value} dp",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}