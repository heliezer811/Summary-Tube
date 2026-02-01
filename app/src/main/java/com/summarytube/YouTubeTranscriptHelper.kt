package com.summarytube

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

object YouTubeTranscriptHelper {
    private val client = OkHttpClient()

    suspend fun fetchTranscript(videoUrl: String): String = withContext(Dispatchers.IO) {
        Log.d("SummaryTube", "fetchTranscript: Iniciando para URL $videoUrl") // Debug
        try {
            val videoId = extractVideoId(videoUrl) ?: return@withContext "ID do vídeo inválido."
            
            // 1. Pegar a página do vídeo para encontrar a URL das legendas
            val request = Request.Builder()
                .url("https://www.youtube.com/watch?v=$videoId")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext "Erro HTTP ao pegar página do vídeo: ${response.code}"
            }
            val html = response.body?.string() ?: return@withContext "Resposta vazia da página do vídeo."
            Log.d("SummaryTube", "fetchTranscript: HTML da página obtido com sucesso") // Debug

            // 2. Localizar a URL do arquivo de legendas (TimedText)
            val captionUrl = findCaptionUrl(html) 
                ?: return@withContext "Não foi possível encontrar legendas para este vídeo."

            // 3. Baixar o XML das legendas e limpar as tags
            val captionRequest = Request.Builder().url(captionUrl).build()
            val captionResponse = client.newCall(captionRequest).execute()
            if (!captionResponse.isSuccessful) {
                return@withContext "Erro HTTP ao pegar legendas: ${captionResponse.code}"
            }
            val xmlText = captionResponse.body?.string() ?: return@withContext "Resposta vazia das legendas."
            Log.d("SummaryTube", "fetchTranscript: XML de legendas obtido com sucesso") // Debug

            return@withContext cleanXmlTranscript(xmlText)
        } catch (e: Exception) {
            Log.e("SummaryTube", "Erro geral em fetchTranscript", e) // Debug erro
            "Erro ao extrair transcrição: ${e.message}. Verifique se o vídeo tem legendas ou rede."
        }
    }

    private fun extractVideoId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%\u200C\u200B2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }

    private fun findCaptionUrl(html: String): String? {
        // Busca o link dentro do JSON 'captionTracks' na página
        val regex = "\"captionTracks\":\\[\\{\"baseUrl\":\"(.*?)\"".toRegex()
        val match = regex.find(html)
        return match?.groupValues?.get(1)?.replace("\\u0026", "&")
    }

    private fun cleanXmlTranscript(xml: String): String {
        // Remove as tags XML <text...> e mantém apenas o texto
        return xml.replace(Regex("<text.*?>"), "")
            .replace("</text>", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("<.*?>"), "") // Limpeza final de qualquer tag
    }
}
