package com.summarytube

import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.*
//import androidx.lifecycle.setViewTreeLifecycleOwner
//import androidx.lifecycle.setViewTreeViewModelStoreOwner
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.*
//import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import android.net.Uri

class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    // Implementações obrigatórias para o Compose no Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    // Aqui recebemos o link vindo do Widget
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SummaryTube", "Service started with action: ${intent?.action}")
        val action = intent?.action
    
        // Captura a posição do widget na tela (se disponível)
        //val bounds = intent?.sourceBounds
        //val yOffset = bounds?.top ?: 100 // Posição vertical do widget

        when (action) {
            "ACTION_OPEN_INPUT" -> showInputOverlay()
            "ACTION_PASTE_AND_SUMMARY" -> {
                val link = getLinkFromClipboard()
                if (link.isNotEmpty()) {
                    showSummaryOverlay(link)
                } else {
                    // Adicione um Toast para debug (import android.widget.Toast)
                    Toast.makeText(this, "Nada no clipboard", Toast.LENGTH_SHORT).show()
                }
            }
            "ACTION_START_FROM_WIDGET" -> {
                val urlFromWidget = intent.getStringExtra("VIDEO_URL") ?: ""
                showSummaryOverlay(urlFromWidget)
            }
            else -> {
                val url = intent?.getStringExtra("VIDEO_URL") ?: ""
                if (url.isNotEmpty()) showSummaryOverlay(url)
            }
        }
        return START_NOT_STICKY
    }

    private fun setupServiceLifecycle() {
        composeView?.let {
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeViewModelStoreOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    // 2. MODO INPUT (Barra transparente com teclado)
    private fun showInputOverlay() {
        // Se já existe um overlay aberto, remove antes de criar outro
        if (composeView != null) {
            windowManager.removeView(composeView)
            composeView = null
        }

        composeView = ComposeView(this).apply {
            setupServiceLifecycle() // função de lifecycle
            setContent {
                var textInput by remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) { focusRequester.requestFocus() } // Abre teclado

                // Layout da barra de digitação
                Box(modifier = Modifier.fillMaxSize().clickable { stopSelf() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = Color(0xFF1E1E1E).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(30.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Type or paste link...", color = Color.Gray) },
                                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White
                                )
                            )
                            // O BOTÃO DE ENVIAR QUE VOCÊ PEDIU
                            IconButton(onClick = { 
                                if (textInput.isNotEmpty()) {
                                    // IMPORTANTE: Primeiro removemos o input para abrir o resumo
                                    //windowManager.removeView(this@apply)
                                    showSummaryOverlay(textInput) 
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_send),
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        
        //setupServiceLifecycle()

        // Helper para criar os parâmetros de posição
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            y = 0 // Posiciona exatamente na altura do widget
        }

        windowManager.addView(composeView, params)
    }
    
    private fun showSummaryOverlay(videoUrl: String) {
        // 1. Remove overlay anterior se existir
        if (composeView != null) {
            try {
                windowManager.removeView(composeView)
            } catch (e: Exception) { }
            composeView = null
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 2. Inicializa Preferências (Resolvendo o erro de Unresolved Reference)
        val prefs = Prefs(this)

        // 3. Cria a View do Compose
        composeView = ComposeView(this).apply {
            //val lifecycleOwner = MyLifecycleOwner()
            //lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            //setViewTreeLifecycleOwner(lifecycleOwner)
            //setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            // Vincula os owners necessários para o Compose funcionar fora de uma Activity
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            //setupServiceLifecycle()

            setContent {
                var visible by remember { mutableStateOf(false) }
                var summaryResult by remember { mutableStateOf("Iniciando processamento...") }
                val scope = rememberCoroutineScope()
                //val focusRequester = remember { FocusRequester() }

                // Efeito que dispara assim que o Canvas aparece
                LaunchedEffect(Unit) {
                    //if (urlFromWidget.isNotEmpty()) {
                    //    focusRequester.requestFocus() // Abre o teclado automaticamente
                    //}
                    visible = true
                    scope.launch {
                        try {
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
                        } catch (e: Exception) {
                            Log.e("SummaryTube", "Erro no summary overlay", e)
                            summaryResult = "Erro ao processar: ${e.message}. Verifique link ou rede."
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
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF1E1E1E),
                        tonalElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Cabeçalho
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_widget),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Summary-Tube", color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                                IconButton(onClick = { stopSelf() }) {
                                    Icon(painterResource(R.drawable.ic_close), null, tint = Color.Gray)
                                }
                            }

                            // Área do Markdown (Onde o resumo aparece)
                            Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                                MarkdownText(markdown = summaryResult, style = TextStyle(color = Color.White))//color = Color.White)
                            }

                            // Botões de Ação
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

        //setupServiceLifecycle()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT, // Altura ajustável
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,//Escurece fundo atraz do overlay
            //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // Permite que o teclado apareça!
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.5f;
            gravity = Gravity.CENTER;  // ← Centralizado na tela
            y = 0;
        }

        windowManager.addView(composeView, params)
    }

    private fun getLinkFromClipboard(): String {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            return item.text?.toString() ?: ""
        }
        return ""
    }

    private fun shareToObsidian(context: Context, content: String) {
        val encodedContent = Uri.encode(content)
        val uri = Uri.parse("obsidian://new?content=$encodedContent")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
        composeView?.let { 
            if (it.isAttachedToWindow) windowManager.removeView(it)
        }
    }

    override fun onBind(intent: Intent?) = null
}
