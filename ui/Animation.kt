package com.achievemeaalk.freedjf.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

enum class AnimationTier {
    Tier1,
    Tier2,
    Tier3
}

@Composable
fun AnimatedScreen(
    tier: AnimationTier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true, // The screen is always visible, the animation is handled by enter/exit transitions
        enter = when (tier) {
            AnimationTier.Tier1 -> fadeIn(animationSpec = tween(300))
            AnimationTier.Tier2 -> slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            AnimationTier.Tier3 -> fadeIn(animationSpec = tween(150))
        },
        exit = when (tier) {
            AnimationTier.Tier1 -> fadeOut(animationSpec = tween(300))
            AnimationTier.Tier2 -> fadeOut(animationSpec = tween(300))
            AnimationTier.Tier3 -> fadeOut(animationSpec = tween(150))
        }
    ) {
        content()
    }
}
