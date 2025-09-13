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
import androidx.compose.foundation.layout.size
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



/*
@Preview(showBackground = true)
@Composable
fun PreviewDrawFace() {
    DrawFace("Hello", "Friend")
}
 */