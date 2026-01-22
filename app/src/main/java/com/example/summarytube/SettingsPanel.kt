package com.example.summarytube

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(onClose: () -> Unit) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        // Espaço vazio (lado direito)
        Spacer(modifier = Modifier.weight(1f))

        // Painel
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFF1E1E1E),
                    RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .padding(16.dp)
        ) {
            Text("Settings", color = Color.White)

            Spacer(Modifier.height(16.dp))

            Text("API:", color = Color.Gray)
            TextField(
                value = "************",
                onValueChange = {},
                enabled = false
            )

            Spacer(Modifier.height(16.dp))

            Text("Ver:", color = Color.Gray)
            Text("gpt-4.0", color = Color.White)

            Spacer(Modifier.height(16.dp))

            Text("prompt:", color = Color.Gray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp))
            )
        }
    }
}
