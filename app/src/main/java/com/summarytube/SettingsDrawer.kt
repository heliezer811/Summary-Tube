package com.summarytube

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawerContent(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    
    // Estados vinculados ao SharedPreferences
    var apiKey by remember { mutableStateOf(prefs.apiKey) }
    var prompt by remember { mutableStateOf(prefs.customPrompt) }
    var selectedModel by remember { mutableStateOf(prefs.selectedModel) }

    // Estado para o Dropdown
    val models = listOf("gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(
                Color(0xFF121212), // Fundo escuro
                RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp) // ARREDONDAR BORDAS DIREITAS
            )
            .padding(24.dp)
    ) {
        // Cabeçalho com Título
        Text(
            text = "Settings",
            fontSize = 26.sp, 
            color = Color.White, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // --- API Key ---
        SettingLabel("API Key:")
        CustomTextField(
            value = apiKey,
            onValueChange = { 
                apiKey = it
                prefs.apiKey = it 
            },
            placeholder = "Enter OpenAI Key",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SELEÇÃO DE MODELO (Dropdown)
        SettingLabel("Model:")
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                border = null // Remove borda padrão
            ) {
                Text(selectedModel, color = Color.White)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model, color = Color.White) },
                        onClick = {
                            selectedModel = model
                            prefs.selectedModel = model
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Custom Prompt ---
        SettingLabel("Custom Prompt:")
        CustomTextField(
            value = prompt,
            onValueChange = { 
                prompt = it
                prefs.customPrompt = it 
            },
            placeholder = "Ex: Resuma este vídeo em tópicos...",
            modifier = Modifier.weight(1f), // Ocupa o espaço restante
            singleLine = false
        )
    }
}

@Composable
fun SettingLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF888888),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF444444)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E1E1E),
            unfocusedContainerColor = Color(0xFF1E1E1E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        )
    )
}
