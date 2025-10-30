package com.achievemeaalk.freedjf.ui.transactions

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.compose.ui.res.stringResource
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.ReceiptParser
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
@Composable
fun ReceiptScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        if (hasPermission) {
            CameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                flashEnabled = flashEnabled,
                isProcessing = isProcessing,
                onProcessingStateChange = { isProcessing = it },
                onResult = { amount, date, seller ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_amount", amount)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_date", date)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_seller", seller)
                    navController.popBackStack()
                }
            )

            // Overlay UI
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.spacingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = stringResource(R.string.toggle_flash_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom instruction card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.spacingLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.spacingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                            Text(
                                stringResource(R.string.processing_receipt),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            Text(
                                stringResource(R.string.point_camera_at_receipt),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                            Text(
                                stringResource(R.string.make_sure_total_is_visible),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        } else {
            // Permission denied state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.camera_permission_required),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                Text(
                    stringResource(R.string.please_grant_camera_permission),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    flashEnabled: Boolean,
    isProcessing: Boolean,
    onProcessingStateChange: (Boolean) -> Unit,
    onResult: (String, Long, String) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasProcessed by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analyzer ->
                        analyzer.setAnalyzer(executor) { imageProxy ->
                            if (hasProcessed || isProcessing) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                onProcessingStateChange(true)
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                textRecognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        val result = ReceiptParser.parseText(visionText)
                                        Log.d("ReceiptScanner", "Parsed text: ${visionText.text}")
                                        Log.d("ReceiptScanner", "Found amount: ${result.totalAmount}")
                                        Log.d("ReceiptScanner", "Found seller: ${result.seller}")
                                        Log.d("ReceiptScanner", "Found date: ${result.date}")

                                        if (result.totalAmount != null && result.totalAmount > 0) {
                                            hasProcessed = true
                                            onResult(
                                                result.totalAmount.toString(),
                                                result.date ?: System.currentTimeMillis(),
                                                result.seller ?: ""
                                            )
                                        }
                                        onProcessingStateChange(false)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ReceiptScanner", "Text recognition failed", e)
                                        onProcessingStateChange(false)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )

                    // Control flash
                    camera.cameraControl.enableTorch(flashEnabled)

                } catch (e: Exception) {
                    Log.e("ReceiptScanner", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            // Update flash when state changes
            // The flash control is handled in the camera binding above
        }
    )
}