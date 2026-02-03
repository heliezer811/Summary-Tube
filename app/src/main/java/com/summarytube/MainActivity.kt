package com.summarytube

import android.util.Log
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.WindowManager
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Edge-to-edge e transparent status bar (com check de versão para evitar crash)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // API 30+
                window.setDecorFitsSystemWindows(false)
            } else {
                // Para Android 10 ou inferior: Usa flags antigas para translucent
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
            // Check de permissão de overlay
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish() // Fecha até conceder
                return  // Sai do onCreate para não tentar setContent sem permissão
            }
        
            Log.d("SummaryTube", "onCreate concluído — abrindo UI") // Debug sucesso
        } catch (e: Exception) {
            Log.e("SummaryTube", "Erro no onCreate", e) // Debug erro
            Toast.makeText(this, "Erro ao iniciar app: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Fecha se erro
            return
        }

        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
    
    var urlText by remember { mutableStateOf("") }
    //var resultText by remember { mutableStateOf("") } // Começa vazio
    var summaryResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Animação do Menu (Rotação 90º conforme a aba abre)
    val rotationAngle by animateFloatAsState(
        targetValue = if (drawerState.isOpen) 90f else 0f,
        label = "MenuRotation"
    )

    // Handler para capturar exceptions unhandled na coroutine (evita crash do app)
    val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("SummaryTube", "Unhandled coroutine exception", exception)
        summaryResult = "Erro inesperado: ${exception.message}. Tente novamente."
        isLoading = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawerContent(onClose = {
                scope.launch { drawerState.close() }
            })
        }
            //{ SettingsDrawerContent() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Summary-Tube", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { if(drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu",
                                modifier = Modifier
                                    .rotate(rotationAngle) // Aplica a animação de rotação
                                    .size(32.dp) // Aumenta o tamanho do ícone Menu
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ÁREA DO CANVAS (Onde o texto aparece)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    //contentAlignment = Alignment.Center
                ) {
                    if (summaryResult.isEmpty() && !isLoading) {
                        // Texto que aparece no meio do canvas (img-app.png)
                        Text(
                            "Summary-Tube",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Resultado com Markdown e botões de ação
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                                MarkdownText(
                                    markdown = if (isLoading) "Processando vídeo..." else summaryResult,
                                    style = TextStyle(color = Color.White)
                                    //color = Color.White
                                )
                            }
                            
                            if (!isLoading) {
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = { copyToClipboard(context, summaryResult) }) {
                                        Icon(painterResource(id = R.drawable.ic_copy), null, tint = Color.Gray)
                                    }
                                    IconButton(onClick = { shareToObsidian(context, summaryResult) }) {
                                        Icon(painterResource(id = R.drawable.ic_obsidian), null, tint = Color.Unspecified)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BARRA DE INPUT (Igual ao Widget)
                LinkInputField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    onPaste = { 
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        urlText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    },
                    onSend = {
                        if (urlText.isNotBlank()) {
                            if (prefs.apiKey.isNotBlank()) {
                                summaryResult = "Erro: Configure a API Key nas settings primeiro."
                                return@LinkInputField
                            }
                            isLoading = true
                            Log.d("SummaryTube", "Iniciando processamento do link: $urlText. API Key: ${prefs.apiKey.take(5)}...") // Debug key (parcial)
                            scope.launch(handler) {  // ← Adicione o handler aqui para capturar unhandled
                                try {
                                    val transcript = YouTubeTranscriptHelper.fetchTranscript(urlText)
                                    Log.d("SummaryTube", "Transcrição obtida: ${transcript.take(100)}") // Debug transcript
                                    if (transcript.startsWith("Erro") || transcript.startsWith("ID do vídeo inválido") || transcript.startsWith("Não foi possível")) {
                                        summaryResult = "### Erro na Transcrição\n$transcript"
                                    } else {
                                        summaryResult = OpenAIService.generateSummary(
                                            transcript, prefs.customPrompt, prefs.apiKey, prefs.selectedModel
                                        )
                                        Log.d("SummaryTube", "Resumo gerado com sucesso") // Debug sucesso
                                    }
                                } catch (e: Exception) {
                                    Log.e("SummaryTube", "Erro no processamento", e) // ← Debug erro
                                    summaryResult = "Erro ao processar: ${e.message}. Verifique link, rede ou API key nas settings."
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// Função Composável da Barra de Entrada
@Composable
fun LinkInputField(value: String, onValueChange: (String) -> Unit, onPaste: () -> Unit, onSend: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Mais comprida verticalmente
            .background(Color(0xFF1E1E1E), RoundedCornerShape(30.dp))
            .padding(12.dp)//(horizontal = 8.dp, vertical = 4.dp),
        //verticalAlignment = Alignment.CenterVertically
    ) {
        //IconButton(onClick = onPaste) {
        //    Icon(painterResource(id = R.drawable.ic_paste), "Paste", tint = Color.Gray)
        //}
        // Parte Superior: Campo de Texto
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Paste link...", color = Color.Gray) },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, 
                unfocusedIndicatorColor = Color.Transparent, 
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Parte Inferior: Ícones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPaste, modifier = Modifier.size(48.dp)) { // Ícone maior
                Icon(
                    painter = painterResource(id = R.drawable.ic_paste), 
                    contentDescription = "Paste", 
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp) // Tamanho do desenho interno
                )
            }
            
            IconButton(onClick = onSend, modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send), 
                    contentDescription = "Send", 
                    tint = Color.Gray,
                    //tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

//        IconButton(onClick = onSend) {
//            Icon(painterResource(id = R.drawable.ic_send), "Send", tint = Color.White)
//        }
//    }
//}

// Funções Auxiliares de Compartilhamento
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = android.content.ClipData.newPlainText("Summary", text)
    clipboard.setPrimaryClip(clip)
}

fun shareToObsidian(context: Context, content: String) {
    val encodedContent = Uri.encode(content)
    val uri = Uri.parse("obsidian://new?content=$encodedContent")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
