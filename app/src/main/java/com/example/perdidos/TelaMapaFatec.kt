package com.example.perdidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perdidos.ui.theme.PerdidosTheme

// ----------------------------------------------------
// MODELOS DE DADOS
// ----------------------------------------------------
data class ApontamentoItem(
    val id: String = "1",
    val titulo: String = "Chaveiro com Chaves",
    val descricao: String = "Chaveiro azul encontrado no corredor do campus",
    val andar: String = "1º Pavimento",
    val localNome: String = "Corredor 1º Andar",
    val pinX: Float = 0.5f, // Posição normalizada (0.0f a 1.0f)
    val pinY: Float = 0.5f,
    val dataHora: String = "Hoje, 14:30",
)

data class ItemAmbiente(
    val nome: String,
    val tipo: String? = null,
    val icon: ImageVector,
    val ehLaboratorio: Boolean = false,
)

// ----------------------------------------------------
// TELA 1: SELECIONAR / APONTAR O LOCAL DO ITEM ACHADO
// ----------------------------------------------------
@Composable
fun TelaSelecionarMapaFatec(
    andarInicial: String = "Térreo",
    localInicial: String? = null,
    onConfirmarLocal: (andar: String, localNome: String, pinX: Float, pinY: Float) -> Unit,
) {
    val andares = listOf("Térreo", "1º Pavimento", "2º Pavimento")
    var andarSelecionado by remember(andarInicial) { mutableStateOf(andarInicial) }

    // Ambientes reais e áreas comuns internas da Fatec Diadema por pavimento
    val salasPorAndar = mapOf(
        "Térreo" to listOf(
            "Corredor Central do Térreo", "Pátio Interno (Convivência)",
            "Sanitário Masc (Térreo)", "Sanitário Fem (Térreo)",
            "Secretaria", "Sala dos Profs", "Recepção / Coordenação",
            "Refeitório", "Aquário (PCs)", "Biblioteca",
            "Planta: Maquiagem", "Planta: Líquidos",
            "Planta: Sólidos", "Planta: Semissólidos",
        ),
        "1º Pavimento" to listOf(
            "Corredor 1º Andar",
            "Lab. Química I", "Lab. Química II",
            "Sanitário Masc (1º Andar)", "Sanitário Fem (1º Andar)",
            "Sala 101", "Sala 102", "Sala 103", "Sala 104",
            "Sala 105", "Sala 106", "Sala 107", "Sala 108",
            "Lab. Instrumental", "Lab. Biológicas",
        ),
        "2º Pavimento" to listOf(
            "Corredor 2º Andar", "Pátio Superior (Convivência)",
            "Sanitário Masc (2º Andar)", "Sanitário Fem (2º Andar)",
            "Sala 201", "Sala 202", "Sala 203", "Sala 204",
            "Sala 205", "Sala 206", "Sala 207",
            "Lab. Informática I", "Lab. Informática II",
        ),
    )

    var localSelecionado by remember(andarSelecionado) {
        mutableStateOf(localInicial ?: salasPorAndar[andarSelecionado]?.first() ?: "Corredor Central do Térreo")
    }

    var pinPos by remember(localSelecionado) {
        mutableStateOf(Pair(0.5f, 0.5f))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        // 1. HEADER COM IDENTIDADE VISUAL
        HeaderPerdidos(subtitulo = "Cadastrar Local do Item Achado")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "1. Escolha o pavimento da Fatec Diadema:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            // 2. SELETOR DE PAVIMENTO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                andares.forEach { andar ->
                    val selecionado = andar == andarSelecionado
                    FilterChip(
                        selected = selecionado,
                        onClick = {
                            andarSelecionado = andar
                            localSelecionado = salasPorAndar[andar]?.first() ?: ""
                            pinPos = Pair(0.5f, 0.5f)
                        },
                        label = { Text(andar) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 3. CHIPS DOS AMBIENTES
            Text(
                text = "2. Selecione o ambiente ou aponte diretamente na planta:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(salasPorAndar[andarSelecionado] ?: emptyList()) { local ->
                    InputChip(
                        selected = local == localSelecionado,
                        onClick = {
                            localSelecionado = local
                            pinPos = Pair(0.5f, 0.5f)
                        },
                        label = { Text(local) },
                    )
                }
            }

            // 4. PLANTA ARQUITETÔNICA FIDEDIGNA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Barra de status da planta
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF1E40AF),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Planta Arquitetônica • $andarSelecionado",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                            )
                        }
                        Surface(
                            color = Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "Corredor Central • Toque p/ Apontar",
                                fontSize = 10.sp,
                                color = Color(0xFF1E40AF),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    // Rolagem suave da planta do andar
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        when (andarSelecionado) {
                            "Térreo" -> PlantaTerreoFidedigna(
                                localSelecionado = localSelecionado,
                                pinPos = pinPos,
                                onLocalClick = {
                                    localSelecionado = it
                                    pinPos = Pair(0.5f, 0.5f)
                                },
                                onApontarCorredor = { yRatio ->
                                    localSelecionado = "Corredor Central do Térreo"
                                    pinPos = Pair(0.5f, yRatio)
                                },
                            )
                            "1º Pavimento" -> PlantaPrimeiroPavimentoFidedigna(
                                localSelecionado = localSelecionado,
                                pinPos = pinPos,
                                onLocalClick = {
                                    localSelecionado = it
                                    pinPos = Pair(0.5f, 0.5f)
                                },
                                onApontarCorredor = { yRatio ->
                                    localSelecionado = "Corredor 1º Andar"
                                    pinPos = Pair(0.5f, yRatio)
                                },
                            )
                            "2º Pavimento" -> PlantaSegundoPavimentoFidedigna(
                                localSelecionado = localSelecionado,
                                pinPos = pinPos,
                                onLocalClick = {
                                    localSelecionado = it
                                    pinPos = Pair(0.5f, 0.5f)
                                },
                                onApontarCorredor = { yRatio ->
                                    localSelecionado = "Corredor 2º Andar"
                                    pinPos = Pair(0.5f, yRatio)
                                },
                                onApontarPatioSuperior = { x, y ->
                                    localSelecionado = "Pátio Superior (Convivência)"
                                    pinPos = Pair(x, y)
                                },
                            )
                        }
                    }
                }
            }

            // 5. BOTÃO DE CONFIRMAR O APONTAMENTO
            Button(
                onClick = {
                    onConfirmarLocal(andarSelecionado, localSelecionado, pinPos.first, pinPos.second)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmar: $andarSelecionado • $localSelecionado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TELA 2: VISUALIZAR ONDE O ITEM FOI ACHADO NO MAPA
// ----------------------------------------------------
@Composable
fun TelaVisualizarItemMapa(
    item: ApontamentoItem,
    onVoltar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E40AF))
                .padding(vertical = 16.dp, horizontal = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOCALIZAÇÃO DO ITEM ACHADO",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "Fatec Diadema • Mapa do Campus",
                        color = Color(0xFFBFDBFE),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Cartão com os detalhes do item achado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E40AF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.titulo,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                        )
                        Text(
                            text = item.descricao,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${item.andar} • ${item.localNome}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${item.dataHora}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }
                }
            }

            // Exibição da Planta com o Pin posicionado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Apontamento: ${item.andar}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                            )
                        }
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "Item marcado no mapa",
                                fontSize = 10.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        when (item.andar) {
                            "Térreo" -> PlantaTerreoFidedigna(
                                localSelecionado = item.localNome,
                                pinPos = Pair(item.pinX, item.pinY),
                                onLocalClick = {},
                                onApontarCorredor = {},
                                modoVisualizacao = true,
                            )
                            "1º Pavimento" -> PlantaPrimeiroPavimentoFidedigna(
                                localSelecionado = item.localNome,
                                pinPos = Pair(item.pinX, item.pinY),
                                onLocalClick = {},
                                onApontarCorredor = {},
                                modoVisualizacao = true,
                            )
                            "2º Pavimento" -> PlantaSegundoPavimentoFidedigna(
                                localSelecionado = item.localNome,
                                pinPos = Pair(item.pinX, item.pinY),
                                onLocalClick = {},
                                onApontarCorredor = {},
                                onApontarPatioSuperior = { _, _ -> },
                                modoVisualizacao = true,
                            )
                        }
                    }
                }
            }

            // Instruções de retirada
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1E40AF),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Para retirar este item, procure a Secretaria da Fatec Diadema e informe o local apontado no mapa.",
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TÉRREO: PLANTA FIDEDIGNA A MAPA_TERREO.XML
// Corredor no meio entre as salas, banheiros lado a lado na esquerda,
// áreas comuns internas (Pátio Interno e Corredor) sem áreas externas.
// ----------------------------------------------------
@Composable
fun PlantaTerreoFidedigna(
    localSelecionado: String,
    pinPos: Pair<Float, Float>?,
    onLocalClick: (String) -> Unit,
    onApontarCorredor: (yRatio: Float) -> Unit,
    modoVisualizacao: Boolean = false,
) {
    Text(
        text = "PLANTA DO TÉRREO (CORREDOR CENTRAL & ÁREAS INTERNAS)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E40AF),
        letterSpacing = 0.5.sp,
    )

    val alturaLinha = 52.dp
    val espacamento = 6.dp
    val totalLinhas = 6
    val alturaTotal = (alturaLinha * totalLinhas) + (espacamento * (totalLinhas - 1))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // PAREDE ESQUERDA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1: Banheiros lado a lado no lado esquerdo!
            BlocoBanheirosLadoALado(
                selecionadoMasc = localSelecionado == "Sanitário Masc (Térreo)",
                selecionadoFem = localSelecionado == "Sanitário Fem (Térreo)",
                onMascClick = { onLocalClick("Sanitário Masc (Térreo)") },
                onFemClick = { onLocalClick("Sanitário Fem (Térreo)") },
                altura = alturaLinha,
            )

            // Linha 2: Sala dos Professores
            CardSalaParalela(
                item = ItemAmbiente("Sala dos Profs", "Docentes", Icons.Default.School),
                selecionado = localSelecionado == "Sala dos Profs",
                onClick = { onLocalClick("Sala dos Profs") },
                altura = alturaLinha,
            )

            // Linha 3: Refeitório / Cantina
            CardSalaParalela(
                item = ItemAmbiente("Refeitório", "Cantina & Alimentação", Icons.Default.Restaurant),
                selecionado = localSelecionado == "Refeitório",
                onClick = { onLocalClick("Refeitório") },
                altura = alturaLinha,
            )

            // Linha 4: Biblioteca
            CardSalaParalela(
                item = ItemAmbiente("Biblioteca", "Acervo & Estudos", Icons.AutoMirrored.Filled.MenuBook),
                selecionado = localSelecionado == "Biblioteca",
                onClick = { onLocalClick("Biblioteca") },
                altura = alturaLinha,
            )

            // Linha 5: Planta Piloto - Maquiagem
            CardSalaParalela(
                item = ItemAmbiente("Planta: Maquiagem", "Cosméticos", Icons.Default.Biotech, ehLaboratorio = true),
                selecionado = localSelecionado == "Planta: Maquiagem",
                onClick = { onLocalClick("Planta: Maquiagem") },
                altura = alturaLinha,
            )

            // Linha 6: Planta Piloto - Sólidos
            CardSalaParalela(
                item = ItemAmbiente("Planta: Sólidos", "Pós e Granulados", Icons.Default.Science, ehLaboratorio = true),
                selecionado = localSelecionado == "Planta: Sólidos",
                onClick = { onLocalClick("Planta: Sólidos") },
                altura = alturaLinha,
            )
        }

        // CORREDOR CENTRAL DO TÉRREO NO MEIO
        CorredorCentralInterativo(
            nomeCorredor = "Corredor Central do Térreo",
            altura = alturaTotal,
            selecionado = localSelecionado == "Corredor Central do Térreo",
            pinY = if (localSelecionado == "Corredor Central do Térreo") pinPos?.second ?: 0.5f else 0.5f,
            onApontar = onApontarCorredor,
            modoVisualizacao = modoVisualizacao,
        )

        // PAREDE DIREITA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1: Secretaria
            CardSalaParalela(
                item = ItemAmbiente("Secretaria", "Atendimento Aluno", Icons.Default.Work),
                selecionado = localSelecionado == "Secretaria",
                onClick = { onLocalClick("Secretaria") },
                altura = alturaLinha,
            )

            // Linha 2: Recepção / Coordenação
            CardSalaParalela(
                item = ItemAmbiente("Recepção / Coordenação", "Atendimento", Icons.Default.Apartment),
                selecionado = localSelecionado == "Recepção / Coordenação",
                onClick = { onLocalClick("Recepção / Coordenação") },
                altura = alturaLinha,
            )

            // Linha 3: Aquário (PCs)
            CardSalaParalela(
                item = ItemAmbiente("Aquário (PCs)", "Lab Informática Aberto", Icons.Default.Computer),
                selecionado = localSelecionado == "Aquário (PCs)",
                onClick = { onLocalClick("Aquário (PCs)") },
                altura = alturaLinha,
            )

            // Linha 4: Pátio Interno de Convivência (Área Comum Interna)
            CardSalaParalela(
                item = ItemAmbiente("Pátio Interno (Convivência)", "Área Comum Coberta", Icons.Default.Groups),
                selecionado = localSelecionado == "Pátio Interno (Convivência)",
                onClick = { onLocalClick("Pátio Interno (Convivência)") },
                altura = alturaLinha,
            )

            // Linha 5: Planta Piloto - Líquidos
            CardSalaParalela(
                item = ItemAmbiente("Planta: Líquidos", "Soluções e Emulsões", Icons.Default.Science, ehLaboratorio = true),
                selecionado = localSelecionado == "Planta: Líquidos",
                onClick = { onLocalClick("Planta: Líquidos") },
                altura = alturaLinha,
            )

            // Linha 6: Planta Piloto - Semissólidos
            CardSalaParalela(
                item = ItemAmbiente("Planta: Semissólidos", "Cremes e Pomadas", Icons.Default.Biotech, ehLaboratorio = true),
                selecionado = localSelecionado == "Planta: Semissólidos",
                onClick = { onLocalClick("Planta: Semissólidos") },
                altura = alturaLinha,
            )
        }
    }
}

// ----------------------------------------------------
// 1º PAVIMENTO: DOIS LABS EM UMA EXTREMIDADE E DOIS NA OUTRA PONTA
// Banheiros lado a lado no lado esquerdo, corredor central no meio.
// ----------------------------------------------------
@Composable
fun PlantaPrimeiroPavimentoFidedigna(
    localSelecionado: String,
    pinPos: Pair<Float, Float>?,
    onLocalClick: (String) -> Unit,
    onApontarCorredor: (yRatio: Float) -> Unit,
    modoVisualizacao: Boolean = false,
) {
    Text(
        text = "1º PAVIMENTO: LABS NAS EXTREMIDADES & SALAS PARALELAS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E40AF),
        letterSpacing = 0.5.sp,
    )

    val alturaLinha = 52.dp
    val espacamento = 6.dp
    val totalLinhas = 7
    val alturaTotal = (alturaLinha * totalLinhas) + (espacamento * (totalLinhas - 1))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // PAREDE ESQUERDA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1 (Extremidade 1): Lab. Química I
            CardSalaParalela(
                item = ItemAmbiente("Lab. Química I", "Química Geral", Icons.Default.Science, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Química I",
                onClick = { onLocalClick("Lab. Química I") },
                altura = alturaLinha,
            )

            // Linha 2: Banheiros lado a lado no lado esquerdo!
            BlocoBanheirosLadoALado(
                selecionadoMasc = localSelecionado == "Sanitário Masc (1º Andar)",
                selecionadoFem = localSelecionado == "Sanitário Fem (1º Andar)",
                onMascClick = { onLocalClick("Sanitário Masc (1º Andar)") },
                onFemClick = { onLocalClick("Sanitário Fem (1º Andar)") },
                altura = alturaLinha,
            )

            // Linhas 3 a 6: Salas teóricas
            CardSalaParalela(
                item = ItemAmbiente("Sala 102", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 102",
                onClick = { onLocalClick("Sala 102") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 104", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 104",
                onClick = { onLocalClick("Sala 104") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 106", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 106",
                onClick = { onLocalClick("Sala 106") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 108", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 108",
                onClick = { onLocalClick("Sala 108") },
                altura = alturaLinha,
            )

            // Linha 7 (Outra ponta): Lab. Instrumental
            CardSalaParalela(
                item = ItemAmbiente("Lab. Instrumental", "Análise Instrumental", Icons.Default.Biotech, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Instrumental",
                onClick = { onLocalClick("Lab. Instrumental") },
                altura = alturaLinha,
            )
        }

        // CORREDOR CENTRAL DO 1º ANDAR NO MEIO
        CorredorCentralInterativo(
            nomeCorredor = "Corredor 1º Andar",
            altura = alturaTotal,
            selecionado = localSelecionado == "Corredor 1º Andar",
            pinY = if (localSelecionado == "Corredor 1º Andar") pinPos?.second ?: 0.5f else 0.5f,
            onApontar = onApontarCorredor,
            modoVisualizacao = modoVisualizacao,
        )

        // PAREDE DIREITA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1 (Extremidade 1): Lab. Química II
            CardSalaParalela(
                item = ItemAmbiente("Lab. Química II", "Química Orgânica", Icons.Default.Science, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Química II",
                onClick = { onLocalClick("Lab. Química II") },
                altura = alturaLinha,
            )

            // Linhas 2 a 6: Salas teóricas paralelas
            CardSalaParalela(
                item = ItemAmbiente("Sala 101", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 101",
                onClick = { onLocalClick("Sala 101") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 103", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 103",
                onClick = { onLocalClick("Sala 103") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 105", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 105",
                onClick = { onLocalClick("Sala 105") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 107", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 107",
                onClick = { onLocalClick("Sala 107") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Hall / Escadas 1º", "Acesso Vertical", Icons.Default.Elevator),
                selecionado = localSelecionado == "Hall / Escadas 1º",
                onClick = { onLocalClick("Hall / Escadas 1º") },
                altura = alturaLinha,
            )

            // Linha 7 (Outra ponta): Lab. Biológicas
            CardSalaParalela(
                item = ItemAmbiente("Lab. Biológicas", "Ciências Biológicas", Icons.Default.Biotech, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Biológicas",
                onClick = { onLocalClick("Lab. Biológicas") },
                altura = alturaLinha,
            )
        }
    }
}

// ----------------------------------------------------
// 2º PAVIMENTO: LABS DE TI NA OUTRA EXTREMIDADE (NO FINAL DO CORREDOR)
// Banheiros lado a lado no lado esquerdo, Pátio Superior no topo.
// ----------------------------------------------------
@Composable
fun PlantaSegundoPavimentoFidedigna(
    localSelecionado: String,
    pinPos: Pair<Float, Float>?,
    onLocalClick: (String) -> Unit,
    onApontarCorredor: (yRatio: Float) -> Unit,
    onApontarPatioSuperior: (x: Float, y: Float) -> Unit,
    modoVisualizacao: Boolean = false,
) {
    // Área de convivência do 2º andar (Pátio Superior)
    Text(
        text = "PÁTIO SUPERIOR (ÁREA ABERTA DO 2º ANDAR)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF854D0E),
        letterSpacing = 0.5.sp,
    )

    AreaAbertaApontavel(
        titulo = "Pátio Superior / Espaço de Convivência",
        subtitulo = "Área aberta de respiro e convivência dos estudantes no 2º andar",
        icon = Icons.Default.Deck,
        corFundo = Color(0xFFFEFCE8),
        corBorda = Color(0xFFFDE047),
        selecionado = localSelecionado == "Pátio Superior (Convivência)",
        pinPos = if (localSelecionado == "Pátio Superior (Convivência)") pinPos else null,
        onApontar = onApontarPatioSuperior,
        modoVisualizacao = modoVisualizacao,
        altura = 85.dp,
        tags = listOf("Bancos de Descanso", "Vista Aberta"),
    )

    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "SALAS PARALELAS & LABS DE TI NA OUTRA EXTREMIDADE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E40AF),
        letterSpacing = 0.5.sp,
    )

    val alturaLinha = 52.dp
    val espacamento = 6.dp
    val totalLinhas = 6
    val alturaTotal = (alturaLinha * totalLinhas) + (espacamento * (totalLinhas - 1))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // PAREDE ESQUERDA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1: Banheiros lado a lado no lado esquerdo!
            BlocoBanheirosLadoALado(
                selecionadoMasc = localSelecionado == "Sanitário Masc (2º Andar)",
                selecionadoFem = localSelecionado == "Sanitário Fem (2º Andar)",
                onMascClick = { onLocalClick("Sanitário Masc (2º Andar)") },
                onFemClick = { onLocalClick("Sanitário Fem (2º Andar)") },
                altura = alturaLinha,
            )

            // Linhas 2 a 5: Salas teóricas
            CardSalaParalela(
                item = ItemAmbiente("Sala 202", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 202",
                onClick = { onLocalClick("Sala 202") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 204", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 204",
                onClick = { onLocalClick("Sala 204") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 206", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 206",
                onClick = { onLocalClick("Sala 206") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Hall / Escadas 2º", "Acesso Vertical", Icons.Default.Elevator),
                selecionado = localSelecionado == "Hall / Escadas 2º",
                onClick = { onLocalClick("Hall / Escadas 2º") },
                altura = alturaLinha,
            )

            // Linha 6 (NA OUTRA EXTREMIDADE / FINAL DO CORREDOR): Lab. Informática I
            CardSalaParalela(
                item = ItemAmbiente("Lab. Informática I", "Software & Dev", Icons.Default.Computer, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Informática I",
                onClick = { onLocalClick("Lab. Informática I") },
                altura = alturaLinha,
            )
        }

        // CORREDOR CENTRAL DO 2º ANDAR NO MEIO
        CorredorCentralInterativo(
            nomeCorredor = "Corredor 2º Andar",
            altura = alturaTotal,
            selecionado = localSelecionado == "Corredor 2º Andar",
            pinY = if (localSelecionado == "Corredor 2º Andar") pinPos?.second ?: 0.5f else 0.5f,
            onApontar = onApontarCorredor,
            modoVisualizacao = modoVisualizacao,
        )

        // PAREDE DIREITA
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(espacamento),
        ) {
            // Linha 1 a 5: Salas teóricas
            CardSalaParalela(
                item = ItemAmbiente("Sala 201", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 201",
                onClick = { onLocalClick("Sala 201") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 203", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 203",
                onClick = { onLocalClick("Sala 203") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 205", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 205",
                onClick = { onLocalClick("Sala 205") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala 207", "Teórica", Icons.Default.Class),
                selecionado = localSelecionado == "Sala 207",
                onClick = { onLocalClick("Sala 207") },
                altura = alturaLinha,
            )
            CardSalaParalela(
                item = ItemAmbiente("Sala de Reuniões", "Docentes", Icons.Default.MeetingRoom),
                selecionado = localSelecionado == "Sala de Reuniões",
                onClick = { onLocalClick("Sala de Reuniões") },
                altura = alturaLinha,
            )

            // Linha 6 (NA OUTRA EXTREMIDADE / FINAL DO CORREDOR): Lab. Informática II
            CardSalaParalela(
                item = ItemAmbiente("Lab. Informática II", "Redes & Banco Dados", Icons.Default.Computer, ehLaboratorio = true),
                selecionado = localSelecionado == "Lab. Informática II",
                onClick = { onLocalClick("Lab. Informática II") },
                altura = alturaLinha,
            )
        }
    }
}

// ----------------------------------------------------
// COMPONENTE: BANHEIROS LADO A LADO DO LADO ESQUERDO
// Os banheiros masc e fem ficam lado a lado na parede esquerda
// ----------------------------------------------------
@Composable
fun BlocoBanheirosLadoALado(
    selecionadoMasc: Boolean,
    selecionadoFem: Boolean,
    onMascClick: () -> Unit,
    onFemClick: () -> Unit,
    modifier: Modifier = Modifier,
    altura: Dp = 52.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(altura),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Sanitário Masculino
        Card(
            onClick = onMascClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selecionadoMasc) Color(0xFFEFF6FF) else Color(0xFFFFFFFF),
            ),
            border = BorderStroke(
                width = if (selecionadoMasc) 2.dp else 1.dp,
                color = if (selecionadoMasc) Color(0xFF1E40AF) else Color(0xFFCBD5E1),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Wc,
                    contentDescription = null,
                    tint = if (selecionadoMasc) Color(0xFF1E40AF) else Color(0xFF64748B),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Masc",
                    fontSize = 11.sp,
                    fontWeight = if (selecionadoMasc) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selecionadoMasc) Color(0xFF1E40AF) else Color(0xFF1E293B),
                )
                if (selecionadoMasc) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        // Sanitário Feminino
        Card(
            onClick = onFemClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selecionadoFem) Color(0xFFEFF6FF) else Color(0xFFFFFFFF),
            ),
            border = BorderStroke(
                width = if (selecionadoFem) 2.dp else 1.dp,
                color = if (selecionadoFem) Color(0xFF1E40AF) else Color(0xFFCBD5E1),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Wc,
                    contentDescription = null,
                    tint = if (selecionadoFem) Color(0xFF1E40AF) else Color(0xFF64748B),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Fem",
                    fontSize = 11.sp,
                    fontWeight = if (selecionadoFem) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selecionadoFem) Color(0xFF1E40AF) else Color(0xFF1E293B),
                )
                if (selecionadoFem) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// CORREDOR CENTRAL INTERATIVO (ÁREA DE CIRCULAÇÃO VERTICAL)
// ----------------------------------------------------
@Composable
fun CorredorCentralInterativo(
    nomeCorredor: String,
    altura: Dp,
    selecionado: Boolean,
    pinY: Float,
    onApontar: (yRatio: Float) -> Unit,
    modifier: Modifier = Modifier,
    modoVisualizacao: Boolean = false,
) {
    Card(
        modifier = modifier
            .width(56.dp)
            .height(altura),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
        ),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) Color(0xFF1E40AF) else Color(0xFFCBD5E1),
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(modoVisualizacao) {
                    if (!modoVisualizacao) {
                        detectTapGestures { offset ->
                            val yRatio = (offset.y / size.height.toFloat()).coerceIn(0.04f, 0.96f)
                            onApontar(yRatio)
                        }
                    }
                }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Eixo central do corredor com fluxo de pedestres
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = if (selecionado) Color(0xFF1E40AF) else Color(0xFF94A3B8),
                    modifier = Modifier.size(15.dp),
                )

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (selecionado) Color(0xFF93C5FD) else Color(0xFFCBD5E1)),
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = if (selecionado) Color(0xFF1E40AF) else Color(0xFF94A3B8),
                    modifier = Modifier.size(15.dp),
                )
            }

            // Alfinete no corredor na posição exata clicada
            if (selecionado) {
                val posY = (maxHeight * pinY - 26.dp).coerceIn(4.dp, maxHeight - 36.dp)
                Box(
                    modifier = Modifier
                        .offset(y = posY)
                        .align(Alignment.TopCenter),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626),
                            shadowElevation = 2.dp,
                        ) {
                            Text(
                                text = if (nomeCorredor.contains("1º")) "Corredor 1º" else if (nomeCorredor.contains("2º")) "Corredor 2º" else "Corredor Térreo",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// CARD DE SALA OU LABORATÓRIO PARALELO
// ----------------------------------------------------
@Composable
fun CardSalaParalela(
    item: ItemAmbiente,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    altura: Dp = 52.dp,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(altura),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) {
                Color(0xFFEFF6FF)
            } else if (item.ehLaboratorio) {
                Color(0xFFF8FAFC)
            } else {
                Color(0xFFFFFFFF)
            },
        ),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) {
                Color(0xFF1E40AF)
            } else if (item.ehLaboratorio) {
                Color(0xFF93C5FD)
            } else {
                Color(0xFFCBD5E1)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 3.dp else 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (selecionado) {
                            Color(0xFFDBEAFE)
                        } else if (item.ehLaboratorio) {
                            Color(0xFFE0E7FF)
                        } else {
                            Color(0xFFF1F5F9)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (selecionado) Color(0xFF1E40AF) else Color(0xFF475569),
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.nome,
                    fontSize = 11.sp,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selecionado) Color(0xFF1E40AF) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.tipo != null) {
                    Text(
                        text = item.tipo,
                        fontSize = 9.sp,
                        color = if (selecionado) Color(0xFF3B82F6) else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (selecionado) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Selecionado",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ----------------------------------------------------
// ÁREA ABERTA APONTÁVEL (PÁTIO SUPERIOR)
// ----------------------------------------------------
@Composable
fun AreaAbertaApontavel(
    titulo: String,
    subtitulo: String,
    icon: ImageVector,
    corFundo: Color,
    corBorda: Color,
    selecionado: Boolean,
    pinPos: Pair<Float, Float>?,
    onApontar: (xRatio: Float, yRatio: Float) -> Unit,
    modifier: Modifier = Modifier,
    modoVisualizacao: Boolean = false,
    altura: Dp = 120.dp,
    tags: List<String> = emptyList(),
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(altura),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) Color(0xFFEFF6FF) else corFundo,
        ),
        border = BorderStroke(
            width = if (selecionado) 2.dp else 1.dp,
            color = if (selecionado) Color(0xFF1E40AF) else corBorda,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 3.dp else 0.dp,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(modoVisualizacao) {
                    if (!modoVisualizacao) {
                        detectTapGestures { offset ->
                            val xRatio = (offset.x / size.width.toFloat()).coerceIn(0.06f, 0.94f)
                            val yRatio = (offset.y / size.height.toFloat()).coerceIn(0.12f, 0.88f)
                            onApontar(xRatio, yRatio)
                        }
                    }
                }
                .padding(10.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selecionado) Color(0xFFDBEAFE) else Color(0x20000000)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selecionado) Color(0xFF1E40AF) else Color(0xFF334155),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = titulo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selecionado) Color(0xFF1E40AF) else Color(0xFF0F172A),
                            )
                            Text(
                                text = subtitulo,
                                fontSize = 10.sp,
                                color = if (selecionado) Color(0xFF3B82F6) else Color(0xFF64748B),
                            )
                        }
                    }

                    if (!modoVisualizacao) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selecionado) Color(0xFFDBEAFE) else Color(0xFFE2E8F0),
                        ) {
                            Text(
                                text = if (selecionado) "Apontado" else "Toque livre",
                                fontSize = 9.sp,
                                color = if (selecionado) Color(0xFF1E40AF) else Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x30FFFFFF),
                                border = BorderStroke(0.5.dp, Color(0x20000000)),
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 9.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                if (!selecionado) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Toque em qualquer ponto desta área para marcar",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                        )
                    }
                }
            }

            // Pin renderizado no local exato do toque
            if (selecionado && pinPos != null) {
                val posX = ((maxWidth * pinPos.first) - 14.dp).coerceIn(4.dp, maxWidth - 36.dp)
                val posY = ((maxHeight * pinPos.second) - 26.dp).coerceIn(4.dp, maxHeight - 36.dp)

                Box(
                    modifier = Modifier.offset(x = posX, y = posY),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626),
                            shadowElevation = 2.dp,
                        ) {
                            Text(
                                text = "Item achado",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin de Localização",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// HEADER COM O DESIGN DO CARTAZ
// ----------------------------------------------------
@Composable
fun HeaderPerdidos(subtitulo: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E40AF))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACHADOS E ",
                    color = Color(0xFF2348C5), // Azul camuflado no fundo azul
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                )
                Text(
                    text = "PERDIDOS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                )
            }
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    color = Color(0xFFBFDBFE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ----------------------------------------------------
// PREVIEWS NO ANDROID STUDIO
// ----------------------------------------------------
@Preview(showBackground = true, name = "Térreo: Fidedigno com Corredor no Meio")
@Composable
fun TelaSelecionarMapaFatecPreview() {
    PerdidosTheme {
        TelaSelecionarMapaFatec(
            andarInicial = "Térreo",
            onConfirmarLocal = { _, _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, name = "1º Pavimento: Labs nas Extremidades")
@Composable
fun TelaPrimeiroPavimentoPreview() {
    PerdidosTheme {
        TelaSelecionarMapaFatec(
            andarInicial = "1º Pavimento",
            onConfirmarLocal = { _, _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, name = "2º Pavimento: Labs TI na Outra Extremidade")
@Composable
fun TelaSegundoPavimentoPreview() {
    PerdidosTheme {
        TelaSelecionarMapaFatec(
            andarInicial = "2º Pavimento",
            onConfirmarLocal = { _, _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, name = "Visualizar Item Achado no Mapa")
@Composable
fun TelaVisualizarItemMapaPreview() {
    PerdidosTheme {
        TelaVisualizarItemMapa(
            item = ApontamentoItem(
                titulo = "Calculadora Científica",
                descricao = "Encontrada no corredor em frente aos laboratórios de química",
                andar = "1º Pavimento",
                localNome = "Corredor 1º Andar",
                pinX = 0.5f,
                pinY = 0.22f,
                dataHora = "Hoje, 10:15",
            ),
            onVoltar = {},
        )
    }
}
