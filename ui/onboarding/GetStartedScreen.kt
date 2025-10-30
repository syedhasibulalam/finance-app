package com.achievemeaalk.freedjf.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.settings.LanguageSettingsSheet
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import com.achievemeaalk.freedjf.ui.theme.Motion
import kotlinx.coroutines.delay
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedScreen(
    onGetStartedClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    var showLanguageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = sheetState
        ) {
            LanguageSettingsSheet(viewModel = settingsViewModel)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (
                title,
                characterImage,
                entertainmentCard,
                diningCard,
                savingsCard,
                groceriesCard,
                subscriptionsCard,
                getStartedButton,
                languageButton
            ) = createRefs()

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(languageButton) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.show()
                            showLanguageSheet = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = stringResource(R.string.change_language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }


            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400, 100)) + slideInVertically(animationSpec = tween(400, 100, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top, margin = 80.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.get_started_title, stringResource(id = R.string.app_name)),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.welcome_to_monefy_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, 200)) + scaleIn(animationSpec = tween(500, 200, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(characterImage) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_character),
                    contentDescription = stringResource(R.string.onboarding_desc_1),
                    modifier = Modifier
                        .size(300.dp)
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(450, 250)) + slideInHorizontally(animationSpec = tween(450, 250, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(entertainmentCard) {
                    top.linkTo(title.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 32.dp)
                }
            ) {
                InfoCard(
                    iconRes = R.drawable.ic_launcher,
                    category = stringResource(R.string.entertainment),
                    amount = "$120",
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = -12f
                            cameraDistance = 8 * density.density
                        }
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 12.dp,
                            offsetY = 8.dp
                        )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(450, 300)) + slideInHorizontally(animationSpec = tween(450, 300, easing = FastOutSlowInEasing), initialOffsetX = { it }),
                modifier = Modifier.constrainAs(diningCard) {
                    top.linkTo(title.bottom, margin = 60.dp)
                    end.linkTo(parent.end, margin = 32.dp)
                }
            ) {
                InfoCard(
                    iconRes = R.drawable.ic_launcher,
                    category = stringResource(R.string.dining),
                    amount = "$830",
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 10f
                            cameraDistance = 8 * density.density
                        }
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 12.dp,
                            offsetY = 8.dp
                        )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(450, 350)) + slideInHorizontally(animationSpec = tween(450, 350, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(savingsCard) {
                    top.linkTo(characterImage.top, margin = 180.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                }
            ) {
                InfoCard(
                    iconRes = R.drawable.ic_launcher,
                    category = stringResource(R.string.savings),
                    amount = "$2400",
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 15f
                            cameraDistance = 8 * density.density
                        }
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 12.dp,
                            offsetY = 8.dp
                        )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(450, 400)) + slideInHorizontally(animationSpec = tween(450, 400, easing = FastOutSlowInEasing), initialOffsetX = { it }),
                modifier = Modifier.constrainAs(groceriesCard) {
                    top.linkTo(characterImage.top, margin = 160.dp)
                    end.linkTo(parent.end, margin = 24.dp)
                }
            ) {
                InfoCard(
                    iconRes = R.drawable.ic_launcher,
                    category = stringResource(R.string.groceries),
                    amount = "$485",
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = -8f
                            cameraDistance = 8 * density.density
                        }
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 12.dp,
                            offsetY = 8.dp
                        )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(450, 450)) + slideInHorizontally(animationSpec = tween(450, 450, easing = FastOutSlowInEasing)),
                modifier = Modifier.constrainAs(subscriptionsCard) {
                    bottom.linkTo(getStartedButton.top, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                InfoCard(
                    iconRes = R.drawable.ic_launcher,
                    category = stringResource(R.string.subscriptions),
                    amount = "$82",
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 5f
                            cameraDistance = 8 * density.density
                        }
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 12.dp,
                            offsetY = 8.dp
                        )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400, 500)) + slideInVertically(animationSpec = tween(400, 500, easing = FastOutSlowInEasing), initialOffsetY = { it }),
                modifier = Modifier
                    .constrainAs(getStartedButton) {
                        bottom.linkTo(parent.bottom, margin = 48.dp)
                        start.linkTo(parent.start, margin = 40.dp) // Sets the side margin
                        end.linkTo(parent.end, margin = 40.dp)     // Sets the side margin

                        width = Dimension.fillToConstraints
                    }
            ) {
                Button(
                    onClick = { onGetStartedClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.get_started_button),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    category: String,
    amount: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 5.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = category,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun Modifier.customShadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    shapeRadius: Dp = 16.dp
) = composed {
    val shadowColor = color.toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()

    this.drawBehind {
        drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparent
            frameworkPaint.setShadowLayer(
                blurRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor
            )
            it.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = shapeRadius.toPx(),
                radiusY = shapeRadius.toPx(),
                paint = paint
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun GetStartedScreenPreview() {
    GetStartedScreen(onGetStartedClick = {})
}
