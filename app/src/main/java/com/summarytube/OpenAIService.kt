package com.summarytube

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OpenAIService {
    private val client = OkHttpClient()
    private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSummary(transcript: String, userPrompt: String, apiKey: String, model: String): String = withContext(Dispatchers.IO) {
        Log.d("SummaryTube", "generateSummary: Iniciando com model $model e key ${apiKey.take(5)}...") // Debug
        try {
            val url = "https://api.openai.com/v1/chat/completions"
            
            // Montamos o corpo da requisição com o prompt e a transcrição
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("messages", arrayOf(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", userPrompt)
                    },
                    JSONObject().apply {
                        put("role", "user")
                        put("content", "Aqui está a transcrição para processar: $transcript")
                    }
                ))
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(jsonBody.toString().toRequestBody(MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string() ?: return@withContext "Resposta vazia da OpenAI."
            Log.d("SummaryTube", "generateSummary: Response recebida com code ${response.code}") // Debug response

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseData)
                jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                "Erro na API OpenAI: ${response.message} (code ${response.code}). Verifique API key ou limite."
            }
        } catch (e: Exception) {
            Log.e("SummaryTube", "Erro geral em generateSummary", e) // Debug erro
            "Erro de conexão com OpenAI: ${e.message}. Verifique rede ou API key."
        }
    }
}
