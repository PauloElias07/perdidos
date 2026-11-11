package com.example.perdidos


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun MapaInterativoFatec(
    pavimentoSelecionado: PavimentoFatec,
    onPavimentoChange: (PavimentoFatec) -> Unit,
    localSelecionado: String,
    coordenadaX: Float?,
    coordenadaY: Float?,
    onLocalSelecionado: (local: String, coordX: Float?, coordY: Float?) -> Unit,
    somenteLeitura: Boolean = false // true quando for apenas visualizar onde foi achado
) {
    val linhasDoAndar = plantaPorPavimento[pavimentoSelecionado] ?: emptyList()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. ABAS DOS 3 PAVIMENTOS (Térreo | 1º Pavimento | 2º Pavimento)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PavimentoFatec.values().forEach { pavimento ->
                val selecionado = pavimento == pavimentoSelecionado
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selecionado) Color(0xFF1E40AF) else Color.Transparent)
                        .clickable(enabled = !somenteLeitura) {
                            onPavimentoChange(pavimento)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pavimento.titulo,
                        color = if (selecionado) Color.White else Color(0xFF334155),
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Legenda rápida
        if (!somenteLeitura) {
            Text(
                text = "💡 Toque em uma sala lateral ou aponte a posição exata no Corredor Central:",
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )
        }

        // 2. ESTRUTURA DA PLANTA (Lado Esquerdo | Corredor Central | Lado Direito)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Topo indicando a direção do corredor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pavimentoSelecionado == PavimentoFatec.TERREO)
                            "⬆️ ENTRADA DO CAMPUS / INÍCIO DO CORREDOR"
                        else
                            "⬆️ OLHANDO PARA ESTE CORREDOR (${pavimentoSelecionado.titulo.uppercase()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }

                // Linhas do Pavimento (Todas as salas que você mapeou!)
                linhasDoAndar.forEach { linha ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // COLUNA DA ESQUERDA (Salas / Labs / Aquário / Biblioteca)
                        BlocoSalaFatec(
                            nomeSala = linha.ladoEsquerdo,
                            selecionada = localSelecionado == linha.ladoEsquerdo && linha.ladoEsquerdo.isNotEmpty(),
                            somenteLeitura = somenteLeitura,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 3.dp),
                            onClick = {
                                onLocalSelecionado(linha.ladoEsquerdo, null, null)
                            }
                        )

                        // COLUNA CENTRAL (CORREDOR COM SUPORTE A COORDENADAS X, Y)
                        TrechoCorredorInterativo(
                            nomeTrecho = linha.trechoCorredor,
                            selecionado = localSelecionado == linha.trechoCorredor,
                            coordX = if (localSelecionado == linha.trechoCorredor) coordenadaX else null,
                            coordY = if (localSelecionado == linha.trechoCorredor) coordenadaY else null,
                            somenteLeitura = somenteLeitura,
                            modifier = Modifier
                                .width(76.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp),
                            onToqueCoordenada = { xRelativo, yRelativo ->
                                onLocalSelecionado(linha.trechoCorredor, xRelativo, yRelativo)
                            }
                        )

                        // COLUNA DA DIREITA (Labs / Refeitório / Banheiros / Salas)
                        BlocoSalaFatec(
                            nomeSala = linha.ladoDireito,
                            selecionada = localSelecionado == linha.ladoDireito && linha.ladoDireito.isNotEmpty(),
                            somenteLeitura = somenteLeitura,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 3.dp),
                            onClick = {
                                onLocalSelecionado(linha.ladoDireito, null, null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlocoSalaFatec(
    nomeSala: String,
    selecionada: Boolean,
    somenteLeitura: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (nomeSala.isEmpty()) {
        // Espaço vazio no corredor
        Spacer(modifier = modifier.height(52.dp))
        return
    }

    // Cores diferenciadas por tipo de ambiente para ficar fácil de achar no olho
    val corFundoPadrao = when {
        nomeSala.contains("Lab", ignoreCase = true) || nomeSala.contains("Maker", ignoreCase = true) -> Color(0xFFEDE9FE) // Roxo claro
        nomeSala.contains("Aquário", ignoreCase = true) || nomeSala.contains("Refeitório", ignoreCase = true) || nomeSala.contains("Biblioteca", ignoreCase = true) -> Color(0xFFDBEAFE) // Azul claro
        nomeSala.contains("Banheiro", ignoreCase = true) -> Color(0xFFFEF3C7) // Amarelo claro
        nomeSala.contains("Escada", ignoreCase = true) || nomeSala.contains("Elevador", ignoreCase = true) -> Color(0xFFF1F5F9) // Cinza circulação
        else -> Color(0xFFF8FAFC)
    }

    val corFundo = if (selecionada) Color(0xFF1E40AF) else corFundoPadrao
    val corTexto = if (selecionada) Color.White else Color(0xFF1E293B)
    val corBorda = if (selecionada) Color(0xFF1D4ED8) else Color(0xFFCBD5E1)

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(corFundo)
            .border(width = if (selecionada) 2.dp else 1.dp, color = corBorda, shape = RoundedCornerShape(10.dp))
            .clickable(enabled = !somenteLeitura) { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selecionada) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Selecionado",
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = nomeSala,
                color = corTexto,
                fontSize = 11.sp,
                fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun TrechoCorredorInterativo(
    nomeTrecho: String,
    selecionado: Boolean,
    coordX: Float?,
    coordY: Float?,
    somenteLeitura: Boolean,
    modifier: Modifier = Modifier,
    onToqueCoordenada: (Float, Float) -> Unit
) {
    var tamanhoBloco by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .heightIn(min = 54.dp)
            .background(if (selecionado) Color(0xFFDBEAFE) else Color(0xFFE2E8F0))
            .border(
                width = 1.dp,
                color = if (selecionado) Color(0xFF1E40AF) else Color(0xFFCBD5E1)
            )
            .onSizeChanged { tamanhoBloco = it }
            .pointerInput(somenteLeitura) {
                if (!somenteLeitura) {
                    detectTapGestures { offset ->
                        val largura = tamanhoBloco.width.coerceAtLeast(1)
                        val altura = tamanhoBloco.height.coerceAtLeast(1)
                        val xRel = (offset.x / largura).coerceIn(0.1f, 0.9f)
                        val yRel = (offset.y / altura).coerceIn(0.1f, 0.9f)
                        onToqueCoordenada(xRel, yRel)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CORREDOR",
            fontSize = 8.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold
        )

        // Se foi marcado um ponto no corredor, desenha o Pin na coordenada exata!
        if (selecionado && coordX != null && coordY != null && tamanhoBloco.width > 0) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Posição no corredor",
                tint = Color(0xFFDC2626), // Pin Vermelho
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(22.dp)
                    .offset {
                        IntOffset(
                            x = (coordX * tamanhoBloco.width - 11.dp.toPx()).roundToInt(),
                            y = (coordY * tamanhoBloco.height - 20.dp.toPx()).roundToInt()
                        )
                    }
            )
        }
    }
}