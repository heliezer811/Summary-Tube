package com.summarytube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var urlText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawerContent()
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Row(verticalAlignment = Alignment.CenterVertically) {
                        // Seletor de modelo simulado no topo
                        Text("Auto", fontSize = 14.sp)
                        Icon(painterResource(id = R.drawable.ic_arrow_down), null)
                    } },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(id = R.drawable.ic_menu), "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Histórico */ }) {
                            Icon(painterResource(id = R.drawable.ic_history), "Histórico")
                        }
                        IconButton(onClick = { /* Editar */ }) {
                            Icon(painterResource(id = R.drawable.ic_edit), "Editar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (resultText.isEmpty()) {
                    Text(
                        "Summary-Tube",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                } else {
                    // Canvas de resultado (Markdown viewer aqui no futuro)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Text(resultText, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Barra de Input Inferior
                InputBar(
                    value = urlText,
                    onValueChange = { urlText = it },
                    onSend = { /* Lógica de extração aqui */ }
                )
            }
        }
    }
}

@Composable
fun InputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Colar */ }) {
            Icon(painterResource(id = R.drawable.ic_paste), "Paste", tint = Color.Gray)
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Paste a link...", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                containerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
        IconButton(
            onClick = onSend,
            modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(12.dp))
        ) {
            Icon(painterResource(id = R.drawable.ic_send), "Send", tint = Color.White)
        }
    }
}
