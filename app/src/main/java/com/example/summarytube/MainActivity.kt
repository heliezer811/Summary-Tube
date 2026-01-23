package com.summarytube

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdown.MarkdownText
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    var resultText by remember { mutableStateOf("") } // Começa vazio
    var isLoading by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { SettingsDrawerContent() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Summary-Tube", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(id = R.drawable.ic_menu), "Menu", tint = Color.White)
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
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ÁREA DO CANVAS (Onde o texto aparece)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (resultText.isEmpty() && !isLoading) {
                        // Texto que aparece no meio do canvas (img-app.png)
                        Text(
                            "Summary-Tube",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2A2A2A)
                        )
                    } else {
                        // Resultado com Markdown e botões de ação
                        Column {
                            Box(modifier = Modifier.weight(1f)) {
                                MarkdownText(
                                    markdown = if (isLoading) "Processando vídeo..." else resultText,
                                    color = Color.White
                                )
                            }
                            
                            if (!isLoading) {
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = { copyToClipboard(context, resultText) }) {
                                        Icon(painterResource(id = R.drawable.ic_copy), null, tint = Color.Gray)
                                    }
                                    IconButton(onClick = { shareToObsidian(context, resultText) }) {
                                        Icon(painterResource(id = R.drawable.ic_obsidian), null, tint = Color.Unspecified)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BARRA DE INPUT (Igual ao Widget)
                InputBar(
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
                                val transcript = YouTubeTranscriptHelper.fetchTranscript(urlText)
                                resultText = OpenAIService.generateSummary(
                                    transcript, prefs.customPrompt, prefs.apiKey, prefs.selectedModel
                                )
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}

// Funções Auxiliares de Compartilhamento
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = android.content.ClipData.newPlainText("Summary", text)
    clipboard.setPrimaryClip(clip)
}

fun shareToObsidian(context: Context, content: String) {
    val uri = Uri.parse("obsidian://new?content=${Uri.encode(content)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
