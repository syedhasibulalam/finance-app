package com.achievemeaalk.freedjf.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

object Motion {
    object Animation {
        val ElementEntrance = tween<Float>(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
        val ListItemEntrance = tween<Float>(
            durationMillis = 250,
            easing = LinearOutSlowInEasing
        )
        val Microinteraction = spring<Float>(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        )
        val DataVisualization = tween<Float>(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
        val ContentSwitch = tween<Float>(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    }
}

