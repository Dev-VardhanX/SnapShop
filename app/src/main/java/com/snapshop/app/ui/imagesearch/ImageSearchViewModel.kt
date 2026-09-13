package com.snapshop.app.ui.imagesearch

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.GeminiVisionRepository
import com.snapshop.app.data.repository.ImageSearchResult
import com.snapshop.app.domain.Product
import com.snapshop.app.domain.RecognizedProductInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImageSearchUiState(
    val imageUri: Uri? = null,
    val bitmap: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val recognizedInfo: RecognizedProductInfo? = null,
    val selectedQuery: String? = null,
    val attemptedQueries: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val error: String? = null
)

class ImageSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiRepository = GeminiVisionRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ImageSearchUiState())
    val uiState: StateFlow<ImageSearchUiState> = _uiState.asStateFlow()

    fun analyzeImageUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            imageUri = uri,
            bitmap = null,
            isAnalyzing = true,
            error = null,
            products = emptyList()
        )

        viewModelScope.launch {
            try {
                val result: ImageSearchResult = geminiRepository.analyzeAndSearch(uri)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    recognizedInfo = result.info,
                    selectedQuery = result.selectedQuery,
                    attemptedQueries = result.attemptedQueries,
                    products = result.products
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message ?: "Failed to analyze image with Gemini AI. Please try again."
                )
            }
        }
    }

    fun analyzeBitmap(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            bitmap = bitmap,
            imageUri = null,
            isAnalyzing = true,
            error = null,
            products = emptyList()
        )

        viewModelScope.launch {
            try {
                val result: ImageSearchResult = geminiRepository.analyzeAndSearchBitmap(bitmap)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    recognizedInfo = result.info,
                    selectedQuery = result.selectedQuery,
                    attemptedQueries = result.attemptedQueries,
                    products = result.products
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message ?: "Failed to analyze image with Gemini AI. Please try again."
                )
            }
        }
    }
}
