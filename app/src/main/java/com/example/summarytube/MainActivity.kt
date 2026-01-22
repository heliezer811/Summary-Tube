package com.example.summarytube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var link by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {

        // 🔹 Título + Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Summary-Tube",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.weight(1f))

            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.clickable {
                    showSettings = true
                }
            )
        }

        // 🔹 Canvas central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
                .background(
                    Color(0xFF2A2A2A),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Text(
                "Summary-Tube",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 🔹 Barra inferior (link)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    Color(0xFF3A3A3A),
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = link,
                onValueChange = { link = it },
                placeholder = { Text("Paste a link...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFD6C6FF), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("↑", color = Color.Black)
            }
        }

        // 🔹 Settings lateral (custom)
        if (showSettings) {
            SettingsPanel(
                onClose = { showSettings = false }
            )
        }
    }
}
