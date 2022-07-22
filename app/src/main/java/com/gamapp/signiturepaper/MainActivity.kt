package com.gamapp.signiturepaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamapp.signaturepaper.*
import com.gamapp.signaturepaper.extensions.getAsBase64
import com.gamapp.signiturepaper.ui.theme.SigniturePaperTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SigniturePaperTheme {
                val state = rememberSignaturePaperState()
                var value by remember {
                    mutableStateOf(0f)
                }
                var stroke by remember {
                    mutableStateOf(10f)
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
                        TemplateColors(
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
                                val base64 = state.getAsBase64()
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (base64 != null) {
                                    File(filesDir, "Image.png").apply {
                                        bitmap.compress(
                                            Bitmap.CompressFormat.PNG,
                                            100,
                                            outputStream()
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .wrapContentHeight()
                        ) {
                            Text(text = "GenerateBitmap")
                        }

                    }
                }
            }
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
fun TemplateColors(
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
fun Points(onSet: (Float) -> Unit) {
    val points = remember {
        listOf(
            2f,
            5f,
            10f,
            12f,
            20f,
            25f,
            30f,
            40f,
        )
    }
    Column(
        modifier = Modifier
            .width(40.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        for (point in points) {
            Point(
                modifier = Modifier
                    .width(40.dp)
                    .heightIn(40.dp)
                    .clickable {
                        onSet(point)
                    }, stroke = point
            )
        }
    }
}


@Composable
fun Point(modifier: Modifier, stroke: Float) {
    val size = with(LocalDensity.current) {
        (stroke * 2).toDp()
    }
    Column(
        modifier = modifier.border(1.dp, Color.Green),
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .border(1.dp, Color.Blue)
                .size(size)
        ) {
            drawCircle(Color.Black, radius = stroke)
        }
        Text(
            text = "$stroke",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}