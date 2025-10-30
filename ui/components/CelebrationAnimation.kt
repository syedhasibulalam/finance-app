package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.achievemeaalk.freedjf.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun CelebrationAnimation(onFinished: () -> Unit) {
    var showAnimation by remember { mutableStateOf(true) }
    if (showAnimation) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.congratulation))
        val progress by animateLottieCompositionAsState(composition)
        Box(modifier = Modifier.fillMaxSize()) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (progress == 1f) {
            showAnimation = false
            onFinished()
        }
    }
}



