package com.snapshop.app.ui.imagesearch

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.GeminiVisionRepository
import com.snapshop.app.data.repository.ImageSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ImageSourceMode {
    CAMERA,
    GALLERY
}

data class ImageSearchUiState(
    val imageUri: Uri? = null,
    val bitmap: Bitmap? = null,
    val sourceMode: ImageSourceMode = ImageSourceMode.CAMERA,
    val isAnalyzing: Boolean = false,
    val analysisPhaseMessage: String = "Analyzing your photo...",
    val error: String? = null
)

class ImageSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiRepository = GeminiVisionRepository(application.applicationContext)
    private var analysisJob: Job? = null

    private val _uiState = MutableStateFlow(ImageSearchUiState())
    val uiState: StateFlow<ImageSearchUiState> = _uiState.asStateFlow()

    fun setPreviewFromUri(uri: Uri, sourceMode: ImageSourceMode) {
        analysisJob?.cancel()
        _uiState.value = ImageSearchUiState(
            imageUri = uri,
            bitmap = null,
            sourceMode = sourceMode,
            isAnalyzing = false,
            error = null
        )
    }

    fun setPreviewFromBitmap(bitmap: Bitmap) {
        analysisJob?.cancel()
        _uiState.value = ImageSearchUiState(
            imageUri = null,
            bitmap = bitmap,
            sourceMode = ImageSourceMode.CAMERA,
            isAnalyzing = false,
            error = null
        )
    }

    fun clearPreview() {
        analysisJob?.cancel()
        _uiState.value = ImageSearchUiState(sourceMode = _uiState.value.sourceMode)
    }

    fun startAnalysis(onComplete: (ImageSearchResult) -> Unit, onFailure: (String) -> Unit) {
        val state = _uiState.value
        if (state.imageUri == null && state.bitmap == null) return
        if (state.isAnalyzing) return

        analysisJob?.cancel()
        _uiState.value = state.copy(
            isAnalyzing = true,
            error = null,
            analysisPhaseMessage = "Analyzing your photo..."
        )

        analysisJob = viewModelScope.launch {
            try {
                val result: ImageSearchResult = when {
                    state.bitmap != null -> geminiRepository.analyzeAndSearchBitmap(state.bitmap)
                    state.imageUri != null -> {
                        _uiState.value = _uiState.value.copy(
                            analysisPhaseMessage = "Finding matching products..."
                        )
                        geminiRepository.analyzeAndSearch(state.imageUri)
                    }
                    else -> error("No image to analyze")
                }
                if (_uiState.value.isAnalyzing) {
                    _uiState.value = _uiState.value.copy(isAnalyzing = false)
                    onComplete(result)
                }
            } catch (e: Exception) {
                if (_uiState.value.isAnalyzing) {
                    val message = when {
                        e is retrofit2.HttpException && e.code() == 429 ->
                            "Gemini API quota exceeded for today. Please check your API key quota or try again tomorrow."
                        e is retrofit2.HttpException && e.code() == 503 ->
                            "AI vision service is experiencing high demand. Please retry in a few moments."
                        e is java.net.SocketTimeoutException ->
                            "Connection timed out. Please check your internet connection and try again."
                        else ->
                            "Couldn't analyze this photo (${e.localizedMessage ?: "Network error"}). Please try again."
                    }
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = message
                    )
                    onFailure(message)
                }
            }
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            analysisPhaseMessage = "Analyzing your photo..."
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
