@Composable
fun SettingsDrawerContent() {
    var apiKey by remember { mutableStateOf("sk-........................... ") }
    var prompt by remember { mutableStateOf("""Extraía a transcrição do vídeo anexado, traduza para português brasileiro se já não estiver em português... [O RESTANTE DO SEU PROMPT AQUI]""") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text("Settings", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("API:", color = Color.Gray)
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            visualTransformation = PasswordVisualTransformation(), // Censura a API
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(containerColor = Color(0xFF1E1E1E))
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("promt:", color = Color.Gray) // Mantendo o erro de digitação da imagem se desejar, ou corrigindo para "prompt"
        TextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = TextFieldDefaults.colors(containerColor = Color(0xFF1E1E1E))
        )
    }
}
