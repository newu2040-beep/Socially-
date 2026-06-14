package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null // e.g. "user" or "model"
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    // Compress a little more to reduce token payload size and speed up API response
    compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

object GeminiApiHelper {
    suspend fun generateCaption(bitmap: Bitmap, filterName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Nostalgic vibes captured in Fuji $filterName. #vintagestyle #grainydays"
        }

        val prompt = """
            Analyze this photo and suggest a gorgeous, highly tailored, emotional, or poetic retro-themed social media caption.
            The user applied a "Fuji $filterName" classic film filter to it. 
            Suggest 3 different styles of captions:
            1. Aesthetic & Poetic (short, dreamy)
            2. Fuji Film Enthusiast (referencing the vintage $filterName grain, tones, or camera feel)
            3. Minimalist (1-5 words with aesthetic hashtags)
            Ensure you format the response cleanly with headings so the user can easily copy and paste their favorite option.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap.toBase64()))
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an expert social media curator, retro aesthetic copywriter, and professional film photographer."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Captured beauty under Fuji $filterName. Classic grains, timeless memories. ✨"
        } catch (e: Exception) {
            e.printStackTrace()
            "Nostalgic frames in Fuji $filterName simulation. #dreamy #retroaesthetic (${e.localizedMessage ?: "error"})"
        }
    }

    suspend fun generateChatResponse(chatHistory: List<Content>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hello! I am your AI Retro Creative Assistant. Please configure your API key in the AI Studio secrets panel to unlock full interactive knowledge about film emulsions, photography guidelines, Fuji simulations, and caption aesthetics! For now, how can I help you design your next shot?"
        }

        val request = GenerateContentRequest(
            contents = chatHistory,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are 'Socially AI Assistant', an expert on classical film cameras, vintage photography (specifically Fuji emulsions like Astia, Provia, Velvia, and Classic Chrome), composition, and modern retro styling. Answer questions playfully, help the user edit their photos better, suggest specific vintage camera settings, and format responses structure-fully with clear points."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I'm listening! Tell me more about your photographic vision."
        } catch (e: Exception) {
            e.printStackTrace()
            "I encountered a small hiccup calling the cloud: ${e.localizedMessage}. Please verify you have internet access and a valid Gemini Key!"
        }
    }
}
