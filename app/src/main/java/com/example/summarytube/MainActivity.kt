package com.example.summarytube

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SummaryTubeApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryTubeApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Estados salvos
    var apiKey by rememberSaveable { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var model by rememberSaveable { mutableStateOf(prefs.getString("model", "gpt-4.0") ?: "gpt-4.0") }
    var prompt by rememberSaveable { mutableStateOf(prefs.getString("prompt", defaultPrompt) ?: defaultPrompt) }
    var link by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { SettingsScreen(apiKey, model, prompt, onSave = { newKey, newModel, newPrompt ->
            apiKey = newKey
            model = newModel
            prompt = newPrompt
            prefs.edit().putString("api_key", newKey).putString("model", newModel).putString("prompt", newPrompt).apply()
        }) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Summary-Tube") },
                    navigationIcon = {
                        IconButton(onClick = { drawerState.open() }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Barra de input (como img-app.png)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Paste a link...") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        // Cola do clipboard
                        val clipboard = LocalClipboardManager.current
                        link = clipboard.getText()?.text ?: ""
                    }) {
                        Text("Paste")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (link.isNotEmpty() && apiKey.isNotEmpty()) {
                            // TODO: Na Parte 3, implementar extração/transcrição/resumo aqui
                            result = "Resultado simulado para $link" // Placeholder
                            showResult = true
                        } else {
                            Toast.makeText(context, "Adicione API key e link", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Send")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Canvas central com animação
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = !showResult,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text("Summary-Tube", style = MaterialTheme.typography.headlineLarge)
                    }
                    AnimatedVisibility(
                        visible = showResult,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(result, modifier = Modifier.padding(16.dp))
                    }
                }

                if (showResult) {
                    Row {
                        val clipboard = LocalClipboardManager.current
                        Button(onClick = { clipboard.setText(AnnotatedString(result)) }) {
                            Text("Copy")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, result)
                                setPackage("md.obsidian") // Pacote do Obsidian
                            }
                            context.startActivity(Intent.createChooser(intent, "Share to Obsidian"))
                        }) {
                            Text("Share to Obsidian")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    apiKey: String,
    model: String,
    prompt: String,
    onSave: (String, String, String) -> Unit
) {
    var newApiKey by remember { mutableStateOf(apiKey) }
    var newModel by remember { mutableStateOf(model) }
    var newPrompt by remember { mutableStateOf(prompt) }
    var showApiKey by remember { mutableStateOf(false) } // Para censurar

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        // API Key censurada
        TextField(
            value = newApiKey,
            onValueChange = { newApiKey = it },
            label = { Text("API:") },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(Icons.Default.Visibility, contentDescription = "Toggle visibility")
                }
            }
        )

        // Modelo
        TextField(
            value = newModel,
            onValueChange = { newModel = it },
            label = { Text("Ver:") }
        )

        // Prompt (área grande)
        TextField(
            value = newPrompt,
            onValueChange = { newPrompt = it },
            label = { Text("Prompt:") },
            modifier = Modifier.height(200.dp),
            maxLines = 10
        )

        Button(onClick = { onSave(newApiKey, newModel, newPrompt) }) {
            Text("Save")
        }
    }
}

val defaultPrompt = """Extraía a transcrição do vídeo anexado, traduza para português brasileiro se já não estiver em português, crie notas detalhadas e estruturadas para alguém que queira compreender o material em profundidade.
Organize o conteúdo em seções claramente numeradas com base nas principais mudanças de tópico ou tema.
Extraia notas estruturadas da transcrição abaixo sem explicação ou prefácio, extraindo os pontos-chave, as ideias principais e os detalhes importantes.
FORMATE USANDO TÍTULOS MARKDOWN CORRETOS com sintaxe # (não use texto em negrito).
Especificamente:
2. Use subtítulos numerados em Markdown (por exemplo, "## 1. Tópico")
3. Use títulos de seção numerados em Markdown (por exemplo, "### 1.1. Subtópico")
4. NÃO use texto em negrito (**texto**) para títulos
5. Use marcadores para listas
Este documento será processado como Markdown para o Obsidian, portanto, a sintaxe correta dos títulos é essencial. Trate-o como um documento para treinamento de futuros analistas nesta área.
Forneça apenas as notas de resumo.
Não explique o que você está fazendo nem inclua nenhuma frase introdutória.
 Seu trabalho deve ser apenas conteúdo Markdown limpo. Não introduza, explique ou narre nada sobre a tarefa. Comece diretamente com o conteúdo.
Inicie o trabalho apenas com o conteúdo resumido, sem cabeçalhos, preâmbulos ou posâmbulos.
Responda apenas com a resposta completa, sem texto introdutório ou final.
Para cada seção:
- Numere as seções sequencialmente (1, 2, 3, etc.). IMPORTANTE: Use a sintaxe de cabeçalho Markdown do Obsidian com símbolos #, não texto em negrito.
- Escreva vários parágrafos detalhados que expliquem o conteúdo e qualquer teoria, termos técnicos ou definições, modelos e estruturas extraídos da transcrição de forma completa e detalhada.
- Inclua abaixo dos parágrafos os principais conceitos, termos, taxonomia, ontologia ou ideias e explique-os claramente com exemplos quando relevante.
- Incorpore e explique citações importantes diretamente do sujeito (pessoa), analogias ou referências.
- Explore o raciocínio, as implicações ou o significado mais amplo por trás das ideias.
 - Identifique e analise explicitamente quaisquer contrastes, tensões, contradições ou mudanças de perspectiva ao longo da discussão. Preste especial atenção às relações dialéticas entre os conceitos.
Comece a resposta imediatamente com um breve parágrafo resumindo os temas principais. Não o rotule nem o descreva.
Ao final, inclua uma seção de conclusão listando todos os livros, pessoas ou recursos mencionados, juntamente com uma breve explicação de sua relevância."""
