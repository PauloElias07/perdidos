package com.example.perdidos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.perdidos.ui.theme.PerdidosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerdidosTheme {
                var tabSelecionada by remember { mutableIntStateOf(0) }
                var itemCadastrado by remember {
                    mutableStateOf(
                        ApontamentoItem(
                            id = "1",
                            titulo = "Chaveiro com Chaves",
                            descricao = "Encontrado perto dos bancos de convivência",
                            andar = "Térreo",
                            localNome = "Pátio Interno (Convivência Aberta)",
                            pinX = 0.52f,
                            pinY = 0.58f,
                            dataHora = "Hoje às 14:30",
                        )
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = tabSelecionada == 0,
                                onClick = { tabSelecionada = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AddLocationAlt,
                                        contentDescription = "Apontar Local"
                                    )
                                },
                                label = { Text("Apontar Local") }
                            )
                            NavigationBarItem(
                                selected = tabSelecionada == 1,
                                onClick = { tabSelecionada = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Ver no Mapa"
                                    )
                                },
                                label = { Text("Ver no Mapa") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        when (tabSelecionada) {
                            0 -> {
                                TelaSelecionarMapaFatec(
                                    andarInicial = itemCadastrado.andar,
                                    localInicial = itemCadastrado.localNome,
                                ) { andar, local, pinX, pinY ->
                                    itemCadastrado = itemCadastrado.copy(
                                        andar = andar,
                                        localNome = local,
                                        pinX = pinX,
                                        pinY = pinY,
                                        dataHora = "Agora mesmo",
                                    )
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Apontamento salvo em $andar - $local!",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    // Alterna automaticamente para visualização do item no mapa
                                    tabSelecionada = 1
                                }
                            }
                            1 -> {
                                TelaVisualizarItemMapa(
                                    item = itemCadastrado,
                                ) {
                                    tabSelecionada = 0
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
