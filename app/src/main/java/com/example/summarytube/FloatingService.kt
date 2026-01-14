package com.example.summarytube

import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.mlkit.nl.languageid.LanguageIdentification

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: LinearLayout

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null) as LinearLayout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val composeView = floatingView.findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            FloatingWidgetContent()
        }

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
    }

    @Composable
    fun FloatingWidgetContent() {
        val context = LocalContext.current
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val model = prefs.getString("model", "gpt-4.0") ?: "gpt-4.0"
        val prompt = prefs.getString("prompt", defaultPrompt) ?: defaultPrompt

        var link by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var showPopup by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        val popupHeight by animateDpAsState(if (showPopup) 400.dp else 0.dp)

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.wrapContentSize()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_widget), contentDescription = "Icon")
                    TextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Paste a link...") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val clipboard = LocalClipboardManager.current
                        link = clipboard.getText()?.text ?: ""
                    }) {
                        Icon(painterResource(android.R.drawable.ic_menu_paste), "Paste")
                    }
                    IconButton(onClick = {
                        if (link.isNotEmpty() && apiKey.isNotEmpty()) {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val transcription = extractTranscription(link)
                                    val detectedLang = detectLanguage(transcription)
                                    val finalPrompt = if (detectedLang != "pt") prompt else prompt.replace("traduza para português brasileiro se já não estiver em português", "")
                                    val summary = generateSummary(transcription, finalPrompt, model, apiKey)
                                    withContext(Dispatchers.Main) {
                                        result = summary
                                        showPopup = true
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Adicione API key e link", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(painterResource(android.R.drawable.ic_menu_send), "Send")
                    }
                }

                AnimatedVisibility(
                    visible = showPopup,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .height(popupHeight)
                            .fillMaxWidth()
                            .animateContentSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column {
                            Icon(painter = painterResource(id = R.drawable.ic_widget), contentDescription = "Icon")
                            Text(result, modifier = Modifier.padding(16.dp))
                            Row {
                                val clipboard = LocalClipboardManager.current
                                Button(onClick = { clipboard.setText(AnnotatedString(result)) }) {
                                    Text("Copy")
                                }
                                Button(onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, result)
                                        setPackage("md.obsidian")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share to Obsidian"))
                                }) {
                                    Text("Share to Obsidian")
                                }
                            }
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }
        }
    }
}
