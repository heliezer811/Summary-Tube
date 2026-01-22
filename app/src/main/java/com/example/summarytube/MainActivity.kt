package com.example.summarytube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var link by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {

        // 🔹 Top bar mais leve
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Summary-Tube",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { showSettings = true }
            )
        }

        // 🔹 Canvas central (mais espaçamento)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 110.dp, bottom = 150.dp, start = 20.dp, end = 20.dp)
                .background(
                    Color(0xFF2A2A2A),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Text(
                "Summary-Tube",
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 🔹 Barra inferior (igual referência)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .height(64.dp)
                .background(
                    Color(0xFF3A3A3A),
                    RoundedCornerShape(40.dp)
                )
                .padding(start = 20.dp, end = 12.dp),
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
                    .size(44.dp)
                    .background(Color(0xFFD6C6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (showSettings) {
            SettingsPanel { showSettings = false }
        }
    }
}
