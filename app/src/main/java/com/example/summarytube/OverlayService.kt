package com.summarytube

import android.app.Service
import android.view.WindowManager
import android.view.Gravity
import android.graphics.PixelFormat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.jeziellago.compose.markdown.MarkdownText

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "SHOW_POPUP") {
            showCanvas()
        }
        return START_NOT_STICKY
    }

    private fun showCanvas() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        composeView = ComposeView(this).apply {
            // Setup de Lifecycle para Compose em Service
            val lifecycleOwner = MyLifecycleOwner()
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true } // Trigger da animação

                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 600.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF1E1E1E),
                        tonalElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Cabeçalho com ic_widget e Título
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_widget),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Summary-Tube",
                                    Modifier.padding(start = 12.dp).weight(1f),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(onClick = { stopSelf() }) {
                                    Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_close), null, tint = Color.Gray)
                                }
                            }

                            // Área de conteúdo Markdown
                            Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                                MarkdownText(
                                    markdown = "### Gerando resumo...\n(Aqui apareceria o texto processado)",
                                    color = Color.LightGray
                                )
                            }

                            // Ações inferiores
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { /* Copiar */ }) {
                                    Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_copy), null)
                                }
                                Button(
                                    onClick = { /* Lógica Obsidian */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                                ) {
                                    Text("Send to Obsidian")
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
            WindowManager.LayoutParams.FLAG_DIM_BEHIND, // Escurece o fundo atrás do popup
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.5f
            gravity = Gravity.CENTER
        }

        windowManager.addView(composeView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?) = null
}
