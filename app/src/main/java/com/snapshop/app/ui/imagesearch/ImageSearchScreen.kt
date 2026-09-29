package com.snapshop.app.ui.imagesearch

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.snapshop.app.data.repository.ImageSearchResult
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchScreen(
    initialMode: String = "CAMERA",
    onBackClick: () -> Unit,
    onVisualSearchComplete: (query: String, result: ImageSearchResult) -> Unit,
    viewModel: ImageSearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var pendingGalleryPick by remember { mutableStateOf(initialMode == "GALLERY") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        pendingGalleryPick = false
        if (uri != null) {
            viewModel.setPreviewFromUri(uri, ImageSourceMode.GALLERY)
        }
    }

    LaunchedEffect(Unit) {
        if (initialMode == "GALLERY") {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val hasPreview = uiState.imageUri != null || uiState.bitmap != null

    BackHandler(enabled = uiState.isAnalyzing) {
        viewModel.cancelAnalysis()
    }

    BackHandler(enabled = hasPreview && !uiState.isAnalyzing) {
        viewModel.clearPreview()
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayIconButton(
                    onClick = {
                        when {
                            uiState.isAnalyzing -> viewModel.cancelAnalysis()
                            hasPreview -> viewModel.clearPreview()
                            else -> onBackClick()
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    size = 42.dp,
                    iconSize = 20.dp
                )

                Text(
                    text = when {
                        uiState.isAnalyzing -> "Identifying..."
                        hasPreview -> "Photo Preview"
                        else -> "Visual Search"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.size(42.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            when {
                // 1. Polished Clay Animated Loading State
                uiState.isAnalyzing -> ClayAnalyzingContent(
                    imageUri = uiState.imageUri,
                    bitmap = uiState.bitmap,
                    onCancel = { viewModel.cancelAnalysis() }
                )

                // 2. Photo Preview State
                hasPreview -> ClayPhotoPreviewContent(
                    imageUri = uiState.imageUri,
                    bitmap = uiState.bitmap,
                    sourceMode = uiState.sourceMode,
                    error = uiState.error,
                    onAnalyze = {
                        viewModel.clearError()
                        viewModel.startAnalysis(
                            onComplete = { result ->
                                onVisualSearchComplete(result.selectedQuery, result)
                            },
                            onFailure = { }
                        )
                    },
                    onRetakeOrChoose = {
                        viewModel.clearPreview()
                        if (uiState.sourceMode == ImageSourceMode.GALLERY) {
                            pendingGalleryPick = true
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                // 3. Camera Viewfinder State
                hasCameraPermission -> ClayCameraViewfinder(
                    lifecycleOwner = lifecycleOwner,
                    onImageCaptureReady = { imageCapture = it },
                    onGalleryClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCapture = {
                        val capture = imageCapture ?: return@ClayCameraViewfinder
                        capture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    try {
                                        val bitmap = imageProxyToBitmap(image)
                                        if (bitmap != null) {
                                            viewModel.setPreviewFromBitmap(bitmap)
                                        }
                                    } finally {
                                        image.close()
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    if (exception.imageCaptureError != ImageCapture.ERROR_CAMERA_CLOSED) {
                                        Log.e("ImageSearchScreen", "Photo capture failed: ${exception.message}", exception)
                                    }
                                }
                            }
                        )
                    }
                )

                // 4. Permission Request State
                else -> ClayPermissionContent(
                    onGrantClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onUseGallery = {
                        pendingGalleryPick = true
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Camera Viewfinder with Clay reticle frame and tactile shutter button.
 */
@Composable
private fun ClayCameraViewfinder(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onGalleryClick: () -> Unit,
    onCapture: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder().build()
                    onImageCaptureReady(capture)
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        Log.e("ImageSearchScreen", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Molded Clay Pill Guide at Top
        ClayCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceLight.copy(alpha = 0.92f),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Center the product in the viewfinder",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
            }
        }

        // Viewfinder Frame Brackets
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
        )

        // Bottom Controls: Gallery & Tactile Shutter Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayIconButton(
                    onClick = onGalleryClick,
                    icon = Icons.Default.PhotoLibrary,
                    contentDescription = "Upload from Gallery",
                    size = 54.dp,
                    iconSize = 24.dp,
                    shape = CircleShape,
                    containerColor = SurfaceLight.copy(alpha = 0.95f),
                    contentColor = PrimaryIndigo
                )

                // Large Tactile Clay Shutter Button
                ClayButton(
                    onClick = onCapture,
                    variant = ClayButtonVariant.Primary,
                    shape = CircleShape,
                    modifier = Modifier.size(78.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photo",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Placeholder space for balance
                Spacer(modifier = Modifier.size(54.dp))
            }
        }
    }
}

/**
 * Clay Photo Preview Screen with analyze & retake actions.
 */
@Composable
private fun ClayPhotoPreviewContent(
    imageUri: Uri?,
    bitmap: Bitmap?,
    sourceMode: ImageSourceMode,
    error: String?,
    onAnalyze: () -> Unit,
    onRetakeOrChoose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ClayCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = SurfaceLight,
            elevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured product photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected product photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(14.dp))
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                elevation = 2.dp
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        ClayButton(
            onClick = onAnalyze,
            text = "Find Matching Products",
            icon = Icons.Default.Search,
            variant = ClayButtonVariant.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ClayButton(
            onClick = onRetakeOrChoose,
            text = if (sourceMode == ImageSourceMode.CAMERA) "Retake Photo" else "Choose Another Photo",
            variant = ClayButtonVariant.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

/**
 * Animated Clay Loading State.
 * Absolutely NO technical model names, JSON, or Gemini references.
 * Soft pulsing clay animation.
 */
@Composable
private fun ClayAnalyzingContent(
    imageUri: Uri?,
    bitmap: Bitmap?,
    onCancel: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing Clay Image Frame
        ClayCard(
            modifier = Modifier
                .size(130.dp)
                .scale(pulseScale),
            shape = RoundedCornerShape(26.dp),
            color = SurfaceLight,
            elevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmap != null -> Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    imageUri != null -> AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    else -> Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Finding products...",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimaryLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Matching your photo with live store listings across India",
            fontSize = 13.sp,
            color = TextSecondaryLight,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        CircularProgressIndicator(
            color = PrimaryIndigo,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        ClayButton(
            onClick = onCancel,
            text = "Cancel",
            variant = ClayButtonVariant.Secondary,
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

/**
 * Camera Permission Request State.
 */
@Composable
private fun ClayPermissionContent(
    onGrantClick: () -> Unit,
    onUseGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ClayCard(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = PrimaryIndigoContainer,
            elevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = PrimaryIndigo
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Camera access needed",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimaryLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SnapShop uses your camera to identify products and discover best prices across stores.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = TextSecondaryLight,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ClayButton(
            onClick = onGrantClick,
            text = "Enable Camera Access",
            variant = ClayButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ClayButton(
            onClick = onUseGallery,
            text = "Choose from Gallery",
            icon = Icons.Default.PhotoLibrary,
            variant = ClayButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    return try {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        Log.e("ImageSearchScreen", "Failed to decode captured image: ${e.message}", e)
        null
    }
}
