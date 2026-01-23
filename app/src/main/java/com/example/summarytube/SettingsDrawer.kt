@Composable
fun SettingsDrawerContent() {
    var apiKey by remember { mutableStateOf("sk-........................... ") }
    var prompt by remember { mutableStateOf("""Extraía a transcrição do vídeo anexado, traduza para português brasileiro se já não estiver em português, crie notas detalhadas e estruturadas para alguém que queira compreender o material em profundidade. 
Organize o conteúdo em seções claramente numeradas com base nas principais mudanças de tópico ou tema.

Extraia notas estruturadas da transcrição abaixo sem explicação ou prefácio, extraindo os pontos-chave, as ideias principais e os detalhes importantes.

FORMATE USANDO TÍTULOS MARKDOWN CORRETOS com sintaxe # (não use texto em negrito).

Especificamente:
1. Use subtítulos numerados em Markdown (por exemplo, "## 1. Tópico")
2. Use títulos de seção numerados em Markdown (por exemplo, "### 1.1. Subtópico")
3. NÃO use texto em negrito (**texto**) para títulos
4. Use marcadores para listas

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

Ao final, inclua uma seção de conclusão listando todos os livros, pessoas ou recursos mencionados, juntamente com uma breve explicação de sua relevância.""") }

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
