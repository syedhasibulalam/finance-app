package com.achievemeaalk.freedjf.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.theme.InactiveOnboardingIndicator
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.achievemeaalk.freedjf.ui.theme.Motion
import kotlinx.coroutines.delay
import androidx.compose.animation.slideInVertically

data class OnboardingItem(
    val titleResId: Int,
    val descriptionResId: Int,
    val lottieResId: Int
)

val onboardingPages = listOf(
    OnboardingItem(
        titleResId = R.string.onboarding_title_1,
        descriptionResId = R.string.onboarding_desc_1,
        lottieResId = R.raw.wallet
    ),
    OnboardingItem(
        titleResId = R.string.onboarding_title_2,
        descriptionResId = R.string.onboarding_desc_2,
        lottieResId = R.raw.transactions
    ),
    OnboardingItem(
        titleResId = R.string.onboarding_title_3,
        descriptionResId = R.string.onboarding_desc_3,
        lottieResId = R.raw.notification
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    navController: NavController
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToName -> {
                    navController.navigate(firstAccountPromptRoute) {
                        popUpTo(onboardingRoute) { inclusive = true }
                    }
                }
                is OnboardingEvent.NavigateToAccounts -> {
                    navController.navigate(firstAccountPromptRoute) {
                        popUpTo(onboardingRoute) { inclusive = true }
                    }
                }
                is OnboardingEvent.NavigateToHome -> {
                    // This case can be simplified or removed if not needed,
                    // as the flow is now linear.
                    navController.navigate("dashboard") {
                        popUpTo(onboardingRoute) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TopSection(
                onSkipClick = { viewModel.onOnboardingFinished() }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(item = onboardingPages[page], pagerState = pagerState, pageIndex = page)
            }

            BottomSection(
                pagerState = pagerState,
                onNextClick = {
                    scope.launch {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            viewModel.onOnboardingFinished()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TopSection(onSkipClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "SkipButtonColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = Dimensions.spacingSmall)
    ) {
        Text(
            text = stringResource(R.string.onboarding_skip_button),
            color = color,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(
                    onClick = onSkipClick,
                    interactionSource = interactionSource,
                    indication = null
                )
                .padding(Dimensions.spacingSmall)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPage(item: OnboardingItem, pagerState: PagerState, pageIndex: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(item.lottieResId))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        visible = pagerState.currentPage == pageIndex
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.spacingHuge)
            .pagerAnimation(pagerState, pageIndex)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500))
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.spacingHuge))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, 150)) + slideInVertically(animationSpec = tween(400, 150, easing = LinearOutSlowInEasing))
        ) {
            Text(
                text = stringResource(id = item.titleResId),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.screenPadding))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, 250)) + slideInVertically(animationSpec = tween(400, 250, easing = LinearOutSlowInEasing))
        ) {
            Text(
                text = stringResource(id = item.descriptionResId),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomSection(pagerState: PagerState, onNextClick: () -> Unit) {
    val buttonTextResId = if (pagerState.currentPage < onboardingPages.size - 1) {
        R.string.onboarding_next_button
    } else {
        R.string.get_started_button
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

  Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = Dimensions.spacingExtraLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PageIndicator(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage
        )

        AnimatedPrimaryButton(
            onClick = onNextClick,
            shape = RoundedCornerShape(Dimensions.cornerRadiusExtraLarge),
            contentPadding = PaddingValues(Dimensions.screenPadding)
        ) {
            AnimatedContent(
                targetState = buttonTextResId,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, 90)) togetherWith
                            fadeOut(animationSpec = tween(90))
                }, label = "ButtonText"
            ) { resId ->
                Text(text = stringResource(id = resId), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else InactiveOnboardingIndicator,
                animationSpec = tween(durationMillis = 300)
            )
            val width by animateDpAsState(
                targetValue = if (isSelected) Dimensions.spacingExtraLarge else Dimensions.spacingMedium,
                animationSpec = tween(durationMillis = 300), label = "IndicatorWidth"
            )
            Box(
                modifier = Modifier
                    .height(Dimensions.spacingMedium)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerAnimation(pagerState: PagerState, pageIndex: Int) = this.graphicsLayer {
    val pageOffset = (
            (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
            ).absoluteValue

    alpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )

    scaleX = lerp(
        start = 0.8f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
    scaleY = lerp(
        start = 0.8f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}
