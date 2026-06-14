package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GeminiApiHelper
import com.example.api.Part
import com.example.data.Album
import com.example.data.SavedPhoto
import com.example.data.SociallyDatabase
import com.example.data.SociallyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

enum class Screen {
    Gallery,     // Browse photos & presets
    EditStudio,  // Filter selection, cropping & fine tuning
    Albums,      // Organizer directories
    ChatBot,     // Creative AI Assistant Studio
    Detail       // Single photo inspect & social share menu
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Preset photography options
data class AestheticPreset(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String
)

class SociallyViewModel(private val repository: SociallyRepository) : ViewModel() {

    // Preset options to populate gallery immediately
    val presets = listOf(
        AestheticPreset(
            id = "preset_tokyo",
            name = "Tokyo Neon Alley",
            description = "High density cyberpunk lights ideal for Classic Chrome or Velvia.",
            imageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?auto=format&fit=crop&q=80&w=600"
        ),
        AestheticPreset(
            id = "preset_sedan",
            name = "Nostalgic Vintage Sedan",
            description = "Earthy copper car in warm sunshine, perfect for Astia skin soft tones.",
            imageUrl = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&q=80&w=600"
        ),
        AestheticPreset(
            id = "preset_record",
            name = "Cozy Record Player",
            description = "Warm shadows on spinning vinyl, perfect for Superia print feeling.",
            imageUrl = "https://images.unsplash.com/photo-1539628390353-35d0859a0f00?auto=format&fit=crop&q=80&w=600"
        ),
        AestheticPreset(
            id = "preset_diner",
            name = "Retro Neon Diner",
            description = "Vibrant magenta signage, perfect for high saturation Velvia slides.",
            imageUrl = "https://images.unsplash.com/photo-1498804103079-a6351b050096?auto=format&fit=crop&q=80&w=600"
        ),
        AestheticPreset(
            id = "preset_coast",
            name = "Foggy Coastal Sunset",
            description = "Moody beach mist under gentle golden beams of light.",
            imageUrl = "https://images.unsplash.com/photo-1510414842594-a61c69b5ae57?auto=format&fit=crop&q=80&w=600"
        ),
        AestheticPreset(
            id = "preset_cafe",
            name = "Classic Coffee Shop",
            description = "Moody baristas and dark mahogany tables, ideal for Acros Monochrome.",
            imageUrl = "https://images.unsplash.com/photo-1481833761820-0509d3217039?auto=format&fit=crop&q=80&w=600"
        )
    )

    // Current app routing state
    private val _currentScreen = MutableStateFlow(Screen.Gallery)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Room persistence observation
    val albumsFlow: StateFlow<List<Album>> = repository.allAlbums.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val photosFlow: StateFlow<List<SavedPhoto>> = repository.allPhotos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingSyncCount: StateFlow<Int> = repository.pendingCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Active photo selected for detail view
    private val _detailPhoto = MutableStateFlow<SavedPhoto?>(null)
    val detailPhoto: StateFlow<SavedPhoto?> = _detailPhoto.asStateFlow()

    // --- WORKSTATION PHOTO ADJUSTMENT STATE ---
    private val _workingUri = MutableStateFlow("")
    val workingUri: StateFlow<String> = _workingUri.asStateFlow()

    private val _workingTitle = MutableStateFlow("Timeless Mood")
    val workingTitle: StateFlow<String> = _workingTitle.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Normal")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _brightness = MutableStateFlow(0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _contrast = MutableStateFlow(1f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()

    private val _saturation = MutableStateFlow(1f)
    val saturation: StateFlow<Float> = _saturation.asStateFlow()

    private val _warmth = MutableStateFlow(0f)
    val warmth: StateFlow<Float> = _warmth.asStateFlow()

    private val _vignette = MutableStateFlow(0f)
    val vignette: StateFlow<Float> = _vignette.asStateFlow()

    private val _sharpness = MutableStateFlow(0f)
    val sharpness: StateFlow<Float> = _sharpness.asStateFlow()

    // --- AI GENERATION STATUS ---
    private val _generatedCaption = MutableStateFlow("")
    val generatedCaption: StateFlow<String> = _generatedCaption.asStateFlow()

    private val _isGeneratingCaption = MutableStateFlow(false)
    val isGeneratingCaption: StateFlow<Boolean> = _isGeneratingCaption.asStateFlow()

    // --- CLOUD SYNC SIMULATION STATE ---
    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _syncCompleted = MutableStateFlow(false)
    val syncCompleted: StateFlow<Boolean> = _syncCompleted.asStateFlow()

    // --- CHAT INTERFACE STATES ---
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to Socially AI Studio! 📸 I'm your retro photography assistant, fully versed in classic Fuji film simulations, exposures, and aesthetic retro captions. Ask me anything!",
                isUser = false
            )
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        // Build initial album list if empty
        viewModelScope.launch {
            repository.allAlbums.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertAlbum(Album(name = "Tokyo Wanderlust", description = "Vibrant night lights & cobalt tones"))
                    repository.insertAlbum(Album(name = "Summer Faded Nostalgia", description = "Sun-streaked memories"))
                    repository.insertAlbum(Album(name = "Earthy Retros", description = "Classic cars, cozy cafes & vinyl"))
                }
            }
        }
    }

    // Navigation triggers
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectPresetOrUpload(uriString: String) {
        _workingUri.value = uriString
        _selectedFilter.value = "Normal"
        _brightness.value = 0f
        _contrast.value = 1f
        _saturation.value = 1f
        _warmth.value = 0f
        _vignette.value = 0f
        _sharpness.value = 0f
        _generatedCaption.value = ""
        navigateTo(Screen.EditStudio)
    }

    fun loadPhotoForDetail(photo: SavedPhoto) {
        _detailPhoto.value = photo
        navigateTo(Screen.Detail)
    }

    // Setters for adjustments
    fun setFilter(name: String) { _selectedFilter.value = name }
    fun setWorkingTitle(title: String) { _workingTitle.value = title }
    fun setBrightness(v: Float) { _brightness.value = v }
    fun setContrast(v: Float) { _contrast.value = v }
    fun setSaturation(v: Float) { _saturation.value = v }
    fun setWarmth(v: Float) { _warmth.value = v }
    fun setVignette(v: Float) { _vignette.value = v }
    fun setSharpness(v: Float) { _sharpness.value = v }

    // --- LOCAL IMAGE PROCESSING BITMAP LOADER ---
    private fun loadBitmapFromUri(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrEmpty()) return null
        return try {
            if (uriString.startsWith("http")) {
                val url = URL(uriString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } else {
                val uri = Uri.parse(uriString)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    source
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // AI Generation Caption
    fun triggerAICaptionGeneration(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingCaption.value = true
            _generatedCaption.value = "AI analyzing photographic composition and palette..."
            val bitmap = loadBitmapFromUri(context, _workingUri.value)
            if (bitmap != null) {
                val result = GeminiApiHelper.generateCaption(bitmap, _selectedFilter.value)
                _generatedCaption.value = result
            } else {
                _generatedCaption.value = "Could not load bitmap to analyze. Here is an aesthetic fallback: \n\n\"Chasing memories through a nostalgic Fuji lens. 🎞️ #fujiinspired #vintagedays\""
            }
            _isGeneratingCaption.value = false
        }
    }

    // Save styled photo into database
    fun savePhotoToAlbum(albumId: Int?, title: String, context: Context, callback: () -> Unit) {
        viewModelScope.launch {
            val photo = SavedPhoto(
                albumId = albumId,
                title = title,
                caption = if (_generatedCaption.value.isNotEmpty() && !_generatedCaption.value.startsWith("AI analyzing")) _generatedCaption.value else null,
                fileUri = _workingUri.value,
                filterName = _selectedFilter.value,
                brightness = _brightness.value,
                contrast = _contrast.value,
                saturation = _saturation.value,
                warmth = _warmth.value,
                vignette = _vignette.value,
                sharpness = _sharpness.value,
                syncStatus = "Pending"
            )
            repository.insertPhoto(photo)
            callback()
        }
    }

    fun deletePhoto(photo: SavedPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
            _currentScreen.value = Screen.Gallery
            _detailPhoto.value = null
        }
    }

    fun createAndInsertAlbum(name: String, description: String) {
        viewModelScope.launch {
            repository.insertAlbum(Album(name = name, description = description))
        }
    }

    fun deleteAlbum(album: Album) {
        viewModelScope.launch {
            repository.deleteAlbum(album)
        }
    }

    // --- TRIGGER CLOUD STUDY SYNC ---
    fun runCloudStorageSync() {
        if (_isCloudSyncing.value) return
        viewModelScope.launch {
            _isCloudSyncing.value = true
            _syncCompleted.value = false
            // Simulate rigorous file upload sequences
            delay(2200)
            repository.markAllAsSynced()
            _isCloudSyncing.value = false
            _syncCompleted.value = true
            delay(3000)
            _syncCompleted.value = false
        }
    }

    // --- CHATTER BOT MULTI TURN SESSIONS ---
    fun askAIShopAssistant(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(text = question, isUser = true)
        _chatHistory.value = _chatHistory.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            // Compile prompt with chat history mapping to REST structure
            val historyContents = _chatHistory.value.map { msg ->
                Content(
                    parts = listOf(Part(text = msg.text)),
                    role = if (msg.isUser) "user" else "model"
                )
            }
            val reply = GeminiApiHelper.generateChatResponse(historyContents)
            _chatHistory.value = _chatHistory.value + ChatMessage(text = reply, isUser = false)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                text = "Welcome back to Socially AI Studio! 📸 Ask me how to achieve specific analog exposures, composition strategies, or Fuji film emulation levels.",
                isUser = false
            )
        )
    }
}

class SociallyViewModelFactory(private val repository: SociallyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SociallyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SociallyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
