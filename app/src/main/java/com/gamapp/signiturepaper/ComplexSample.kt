package com.gamapp.signiturepaper

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gamapp.signaturepaper.SignaturePaper
import com.gamapp.signaturepaper.SignaturePaperColors
import com.gamapp.signaturepaper.rememberSignaturePaperState
import com.gamapp.signiturepaper.ui.theme.SigniturePaperTheme
import kotlinx.coroutines.launch

@Composable
fun ComplexSample(){
    SigniturePaperTheme {
        PermissionScreen {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val state = rememberSignaturePaperState()
            var value by remember {
                mutableStateOf(0f)
            }
            var stroke by remember {
                mutableStateOf(1.5.dp)
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
                            .width(30.dp + 50.dp * (value))
                            .fillMaxHeight(),
                        colors = palette,
                        onSelect = {
                            signatureColors = it
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(
                        5.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        onClick = state::clear,
                        modifier = Modifier
                            .wrapContentHeight()
                    ) {
                        Text(text = "Clear")
                    }
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
                        Text(text = "Save")
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    val nextAlpha by remember {
                        derivedStateOf { if (state.canSkipToNext) 1f else 0.5f }
                    }
                    Button(onClick = state::roundSignature) {
                        Text(text = "beautify")
                    }
                    Button(
                        onClick = state::skipToNext,
                        modifier = Modifier
                            .wrapContentHeight()
                            .graphicsLayer {
                                alpha = nextAlpha
                            }
                    ) {
                        Text(text = "Next")
                    }
                    val prevAlpha by remember {
                        derivedStateOf { if (state.canSkipToPrevious) 1f else 0.5f }
                    }
                    Button(
                        onClick = state::skipToPrevious,
                        modifier = Modifier
                            .wrapContentHeight()
                            .graphicsLayer {
                                alpha = prevAlpha
                            }
                    ) {
                        Text(text = "Prev")
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
    LazyColumn(
        modifier = Modifier
            .width(50.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        items(points) { point ->
            val color by remember(point) {
                derivedStateOf {
                    if (selected == point) Color.Blue.copy(0.3f)
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
                    .heightIn(50.dp),
                stroke = point
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