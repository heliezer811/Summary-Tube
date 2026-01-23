package com.summarytube

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDrawerContent() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    
    // Estados vinculados ao que está salvo no celular
    var apiKey by remember { mutableStateOf(prefs.apiKey) }
    var prompt by remember { mutableStateOf(prefs.customPrompt) }
    var selectedModel by remember { mutableStateOf(prefs.selectedModel) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xFF121212))
            .padding(24.dp)
    ) {
        Text("Settings", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Configuração de API
        Text("API Key:", color = Color.Gray, fontSize = 14.sp)
        TextField(
            value = apiKey,
            onValueChange = { 
                apiKey = it
                prefs.apiKey = it // Salva na hora
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                containerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configuração de Modelo
        Text("Model:", color = Color.Gray, fontSize = 14.sp)
        // Simplificado para um campo de texto, mas você pode expandir para um Dropdown
        TextField(
            value = selectedModel,
            onValueChange = { 
                selectedModel = it
                prefs.selectedModel = it 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(containerColor = Color(0xFF1E1E1E))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configuração do Prompt
        Text("Prompt:", color = Color.Gray, fontSize = 14.sp)
        TextField(
            value = prompt,
            onValueChange = { 
                prompt = it
                prefs.customPrompt = it 
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = TextFieldDefaults.colors(
                containerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}
