package com.example.summarytube

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.summarytube.ui.theme.SummaryTubeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SummaryTubeTheme {
                AppScreen(
                    onOpenFloating = {
                        startService(
                            Intent(this, FloatingService::class.java)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(onOpenFloating: () -> Unit) {

    var link by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerContent = {
            SettingsDrawer()
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Summary-Tube") },
                    navigationIcon = {
                        IconButton(onClick = { /* drawer é automático */ }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Barra de link (igual à imagem)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.DarkGray,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = link,
                        onValueChange = { link = it },
                        placeholder = { Text("Paste a link...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            showResult = true
                            onOpenFloating()
                        }
                    ) {
                        Text("↑")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Canvas (resultado)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF2B2B2B),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    if (!showResult) {
                        Text(
                            "Summary-Tube",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Text(
                            "Resultado do resumo aparecerá aqui.",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDrawer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Text("API:")
        TextField(
            value = "**************",
            onValueChange = {},
            enabled = false
        )

        Spacer(Modifier.height(16.dp))

        Text("Ver:")
        Text("gpt-4.0")

        Spacer(Modifier.height(24.dp))

        Text("prompt:")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black, RoundedCornerShape(12.dp))
        )
    }
}
