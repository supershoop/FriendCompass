package com.example.friendcompass4

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

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
    val compassImg = painterResource(R.drawable.friend)
    Image(
        painter = compassImg,
        contentDescription = "compass image",
        modifier = Modifier.size(50.dp).rotate(rotation),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun DrawFriend(message: String, from: String, modifier: Modifier = Modifier, x: Float, y: Float) {
    val friendImg = painterResource(R.drawable.friend)
    Image(
        painter = friendImg,
        contentDescription = "riend image",
        modifier = Modifier.size(40.dp).offset(x.dp, y.dp)
    )
}



@Composable
fun DrawBG(message: String, from: String, modifier: Modifier = Modifier, rotation: Float) {
    val bgImg = painterResource(R.drawable.bg)
    Image(
        painter = bgImg,
        contentDescription = "bg image",
        modifier = Modifier.fillMaxHeight().rotate(rotation),
        contentScale = ContentScale.Crop
    )
    Log.d("ewf", "hi the draw face is activsted")
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewDrawFace() {
    DrawFace("Hello", "Friend")
}
 */