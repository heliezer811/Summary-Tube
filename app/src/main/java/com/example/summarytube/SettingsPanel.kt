package com.example.summarytube

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(onClose: () -> Unit) {

    Row(modifier = Modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFF1C1C1C),
                    RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                )
                .padding(20.dp)
        ) {

            Text(
                "Settings",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            Text("API:", color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFF3D3A45), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "***************",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text("Ver:", color = Color.Gray)
            Text("gpt-4.0", color = Color.White)

            Spacer(Modifier.height(20.dp))

            Text("prompt:", color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black, RoundedCornerShape(20.dp))
            )
        }
    }
}
