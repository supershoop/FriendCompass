package com.example.friendcompass4

import android.R.attr.rotation
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.friendcompass4.R
import android.util.Log
import androidx.annotation.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.drawscope.withTransform


@Composable
fun DrawFace(message: String, from: String, modifier: Modifier = Modifier) {
    val faceImg = painterResource(R.drawable.face)
    Image(
        painter = faceImg,
        contentDescription = "Face image",
        modifier = Modifier.size(50.dp)
    )
    Log.d("ewf", "hi the draw face is activsted")
}

@Composable
fun DrawCompass(message: String, from: String, modifier: Modifier = Modifier, rotation: Float) {
    val compassImg = painterResource(R.drawable.compass)
    Image(
        painter = compassImg,
        contentDescription = "compass image",
        modifier = Modifier.size(70.dp).rotate(rotation),
        contentScale = ContentScale.FillHeight
    )
}

@Composable
fun DrawFriend(message: String, from: String, modifier: Modifier = Modifier, x: Float, y: Float) {
    val friendImg = painterResource(R.drawable.friend)
    Image(
        painter = friendImg,
        contentDescription = "riend image",
        modifier = Modifier.size(30.dp).offset(x.dp, y.dp).clickable {
            //tracking this one

        }
    )
}
@Composable
fun DrawHalo(message: String, from: String, modifier: Modifier = Modifier, x: Float, y: Float) {
    val haloImg = painterResource(R.drawable.halo)
    Image(
        painter = haloImg,
        contentDescription = "riend image",
        modifier = Modifier.size(50.dp).offset(x.dp, y.dp)
    )
}

@Composable
fun DrawTrack() {
    val trackImg = painterResource(R.drawable.tracking)
    Image(
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
        painter = trackImg,
        contentDescription = "friend image",
        //modifier = Modifier.fillMaxSize(),
    )
}


@Composable
fun DrawBG(message: String, from: String, modifier: Modifier = Modifier, rotation: Float) {
    OversizedCanvasImage(rotation)
}

@Composable
fun OversizedCanvasImage(rotat : Float) {
    val img = ImageBitmap.imageResource(R.drawable.bg)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasCenter = Offset(size.width / 2, size.height / 2)

        // Draw image larger than the screen
        withTransform({
            // Rotate around the canvas center
            rotate(degrees = rotat, pivot = canvasCenter)
        }) {
            // Draw image oversized
            drawImage(
                image = img,
                dstSize = IntSize(3000, 3000),   // oversized
                dstOffset = IntOffset(
                    (canvasCenter.x - 1500).toInt(),
                    (canvasCenter.y - 1500).toInt()
                )
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewDrawFace() {
    DrawFace("Hello", "Friend")
}
 */