package com.summarytube

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.os.Build
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
            startActivity(intent)
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
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
            //topBar = {
                //TopAppBar(
                    //title = { Text("Summary-Tube", color = Color.White) },
                    //navigationIcon = {
                    IconButton(onClick = { scope.launch { if(drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier
                                .rotate(rotationAngle) // Aplica a animação de rotação
                                .size(32.dp) // Aumenta o tamanho do ícone do menu
                        )
                        //}
                    }
                    //colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                }

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
                        if (urlText.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val transcript = YouTubeTranscriptHelper.fetchTranscript(urlText)
                                    summaryResult = OpenAIService.generateSummary(
                                        transcript, prefs.customPrompt, prefs.apiKey, prefs.selectedModel
                                    )
                                } catch (e: Exception) {
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
                    tint = Color.White,
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
