package com.summarytube

import android.app.Service
import android.content.Intent
import android.view.WindowManager
import android.graphics.PixelFormat
import android.view.Gravity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.jeziellago.compose.markdown.MarkdownText
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    // Aqui recebemos o link vindo do Widget
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUrl = intent?.getStringExtra("VIDEO_URL") ?: ""
        showCanvas(videoUrl)
        return START_NOT_STICKY
    }

    private fun showCanvas(videoUrl: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val prefs = Prefs(this) // Carrega sua API Key e Prompt salvos
        
        composeView = ComposeView(this).apply {
            val lifecycleOwner = MyLifecycleOwner()
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                var visible by remember { mutableStateOf(false) }
                var summaryResult by remember { mutableStateOf("Iniciando processamento...") }
                val scope = rememberCoroutineScope()

                // Efeito que dispara assim que o Canvas aparece
                LaunchedEffect(Unit) {
                    visible = true
                    scope.launch {
                        summaryResult = "Extraindo transcrição do YouTube..."
                        val transcript = YouTubeTranscriptHelper.fetchTranscript(videoUrl)
                        
                        if (transcript.startsWith("Erro")) {
                            summaryResult = "### Erro na Transcrição\n$transcript"
                        } else {
                            summaryResult = "Transcrição obtida. Gerando resumo com IA..."
                            summaryResult = OpenAIService.generateSummary(
                                transcript, 
                                prefs.customPrompt, 
                                prefs.apiKey, 
                                prefs.selectedModel
                            )
                        }
                    }
                }

                // O visual do Canvas (Popup)
                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn() + fadeIn()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 600.dp).padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        color = Color(0xFF1E1E1E),
                        tonalElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Cabeçalho
                            Row {
                                Text("Summary-Tube", color = Color.White, modifier = Modifier.weight(1f))
                                IconButton(onClick = { stopSelf() }) {
                                    Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_close), null, tint = Color.Gray)
                                }
                            }

                            // Área do Markdown (Onde o resumo aparece)
                            Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                                MarkdownText(markdown = summaryResult, color = Color.White)
                            }

                            // Botões de Ação
                            // Dentro do Column do Surface no OverlayService.kt
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botão de Copiar (Apenas ícone)
                                IconButton(onClick = { copyToClipboard(this@OverlayService, summaryResult) }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_copy),
                                        contentDescription = "Copy",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Botão do Obsidian (Ícone com ação de envio)
                                IconButton(onClick = { shareToObsidian(this@OverlayService, summaryResult) }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_obsidian),
                                        contentDescription = "Send to Obsidian",
                                        tint = Color.Unspecified, // Mantém as cores originais do ícone se for colorido
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,//Escurece fundo atraz do overlay
            PixelFormat.TRANSLUCENT
        ).apply { dimAmount = 0.5f; gravity = Gravity.CENTER }

        windowManager.addView(composeView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?) = null
}
