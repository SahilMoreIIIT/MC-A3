package com.example.myaccelerometer

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun homescreen() {
    Image(
        painter= painterResource(id = R.drawable.background2),
        contentDescription = null,
        contentScale= ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
}