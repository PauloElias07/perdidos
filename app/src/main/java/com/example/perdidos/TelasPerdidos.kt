package com.example.perdidos

import android.Manifest
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// =========================================================================
// HEADER PADRÃO ("ACHADOS" azul no azul e "PERDIDOS" branco)
// =========================================================================
@Composable
fun HeaderPerdidos(
    mostrarBotaoVoltar: Boolean = false,
    onVoltarClick: () -> Unit = {},
    onConfigClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E40AF))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (mostrarBotaoVoltar) {
            IconButton(onClick = onVoltarClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        // O Easter Egg do cartaz da faculdade com o "e" eletrônico!
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ACHADOS",
                color = Color(0xFF2851D8), // Azul quase invisível no fundo azul!
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "e-PERDIDOS",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 1.sp
            )
        }

        IconButton(onClick = onConfigClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurar Lembretes",
                tint = Color.White
            )
        }
    }
}

// =========================================================================
// TELA 1: HOME ("Você perdeu algo?" + Contagem + Cadastrar Item)
// =========================================================================
@Composable
fun TelaHomePerdidos(
    quantidadeItensPerdidos: Int,
    periodoLembreteAtual: String,
    onIrParaCatalogo: () -> Unit,
    onIrParaCadastro: () -> Unit,
    onAbrirConfiguracoes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        HeaderPerdidos(
            mostrarBotaoVoltar = false,
            onConfigClick = onAbrirConfiguracoes
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: "Você perdeu algo?" + Contagem ligada ao Banco
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Você perdeu algo?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    // Círculo com a contagem igual ao seu Figma!
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE))
                                .border(2.dp, Color(0xFF93C5FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = quantidadeItensPerdidos.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E40AF)
                            )
                        }

                        Column {
                            Text(
                                text = if (quantidadeItensPerdidos == 1)
                                    "item aguardando dono"
                                else
                                    "itens aguardando donos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Campus Fatec Diadema",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Só redireciona para o Catálogo se a contagem for > 0!
                    Button(
                        onClick = onIrParaCatalogo,
                        enabled = quantidadeItensPerdidos > 0,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E40AF)
                        )
                    ) {
                        Text(
                            text = if (quantidadeItensPerdidos > 0)
                                "Ver Itens Perdidos →"
                            else
                                "Nenhum item registrado",
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // CARD 2: Embaixo da contagem -> Cadastrar Item Encontrado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF1E40AF),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Achou algum objeto?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Ajude o dono registrando a foto e o local",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onIrParaCadastro,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar Item Encontrado",
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }

            // Info do lembrete configurado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAbrirConfiguracoes() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF1E40AF)
                    )
                    Text(
                        text = "Lembrete: A cada $periodoLembreteAtual (Toque para ajustar)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

// =========================================================================
// TELA 2: CATÁLOGO DE ITENS PERDIDOS (Grade de 2 Colunas)
// =========================================================================
@Composable
fun TelaCatalogoPerdidos(
    itens: List<ItemPerdido>,
    onItemClick: (ItemPerdido) -> Unit,
    onVoltar: () -> Unit,
    onAbrirConfiguracoes: () -> Unit
) {
    var categoriaSelecionada by remember { mutableStateOf("Todos") }
    var busca by remember { mutableStateOf("") }

    val itensFiltrados = itens.filter { item ->
        val bateCategoria = categoriaSelecionada == "Todos" || item.categoria == categoriaSelecionada
        val bateBusca = busca.isBlank() ||
                item.titulo.contains(busca, ignoreCase = true) ||
                item.categoria.contains(busca, ignoreCase = true) ||
                item.subcategoria.contains(busca, ignoreCase = true) ||
                item.localEncontrado.contains(busca, ignoreCase = true)
        bateCategoria && bateBusca
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        HeaderPerdidos(
            mostrarBotaoVoltar = true,
            onVoltarClick = onVoltar,
            onConfigClick = onAbrirConfiguracoes
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Barra de busca
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar item, sala ou tipo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filtros horizontais de categoria
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoriasPerdidos) { cat ->
                    val emoji = if (cat == "Todos") "🔍" else emojiPorCategoria(cat)
                    FilterChip(
                        selected = categoriaSelecionada == cat,
                        onClick = { categoriaSelecionada = cat },
                        label = { Text("$emoji $cat") }
                    )
                }
            }

            if (itensFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "📦", fontSize = 42.sp)
                        Text(
                            text = "Nenhum item encontrado",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Tente outra categoria ou termo de busca",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                // Grade de 2 colunas
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(itensFiltrados, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                // Área da foto do item (foto real ou emoji)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ImagemCloudinaryOuEmoji(
                                        fotoUrl = item.fotoUrl,
                                        categoria = item.categoria,
                                        subcategoria = item.subcategoria,
                                        tamanhoEmoji = 40.sp
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = item.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1
                                    )
                                    if (item.subcategoria.isNotBlank() && item.subcategoria != item.titulo) {
                                        Text(
                                            text = item.subcategoria,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E40AF),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "📍 ${item.pavimento.titulo} • ${item.localEncontrado}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 1
                                    )

                                    if (item.entregueNaCoordenacao) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "✓ Na Coordenação",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TELA 3: CADASTRAR ITEM ("Onde você encontrou?" + Foto e Categorias)
// =========================================================================
@Composable
fun TelaCadastrarItem(
    onSalvarItem: (ItemPerdido) -> Unit,
    onVoltar: () -> Unit,
    onAbrirConfiguracoes: () -> Unit
) {
    var tituloItem by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Item Pessoal") }
    var subcategoria by remember { mutableStateOf("Carteirinha") }
    var pavimento by remember { mutableStateOf(PavimentoFatec.TERREO) }
    var localMarcado by remember { mutableStateOf("Aquário (PCs)") }
    var coordX by remember { mutableStateOf<Float?>(null) }
    var coordY by remember { mutableStateOf<Float?>(null) }
    var entregueCoordenacao by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var fotoCapturadaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var urlFotoCloudinary by remember { mutableStateOf("") }
    var enviandoFoto by remember { mutableStateOf(false) }

    // Abre a câmera do celular e recebe a foto tirada
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoCapturadaBitmap = bitmap
            enviandoFoto = true
            escopo.launch {
                val resultado = CloudinaryService.enviarFotoParaCloudinary(bitmap)
                enviandoFoto = false
                resultado.onSuccess { urlGerada ->
                    urlFotoCloudinary = urlGerada
                    Toast.makeText(context, "Foto salva na nuvem!", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Foto salva localmente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Pede a permissão de câmera antes de abrir
    val permissaoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permissão necessária para fotografar", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        HeaderPerdidos(
            mostrarBotaoVoltar = true,
            onVoltarClick = onVoltar,
            onConfigClick = onAbrirConfiguracoes
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Foto do Item com Conferência Visual
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Foto do Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )

                    // Área visual da foto: mostra a foto real para a pessoa conferir
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(
                                width = 1.dp,
                                color = if (fotoCapturadaBitmap != null) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { permissaoCameraLauncher.launch(Manifest.permission.CAMERA) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoCapturadaBitmap != null) {
                            Image(
                                bitmap = fotoCapturadaBitmap!!.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Barra sobreposta no rodapé da imagem
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (enviandoFoto) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Salvando na nuvem...",
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = if (urlFotoCloudinary.isNotBlank()) "✓ Foto na nuvem" else "✓ Foto capturada",
                                            color = Color(0xFF86EFAC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Toque para alterar",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = Color(0xFF1E40AF),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Fotografar item",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Ajuda o dono a identificar o objeto",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Seleção da Categoria Genérica
                    Text(
                        text = "Categoria",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mapaCategoriasSubcategorias.keys.toList()) { cat ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = {
                                    categoria = cat
                                    val opcoesSub = mapaCategoriasSubcategorias[cat] ?: emptyList()
                                    subcategoria = opcoesSub.firstOrNull() ?: ""
                                },
                                label = { Text("${emojiPorCategoria(cat)} $cat") }
                            )
                        }
                    }

                    // Seleção da Subcategoria
                    val subcategoriasDisponiveis = mapaCategoriasSubcategorias[categoria] ?: emptyList()
                    if (subcategoriasDisponiveis.isNotEmpty()) {
                        Text(
                            text = "Subcategoria",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subcategoriasDisponiveis) { sub ->
                                FilterChip(
                                    selected = subcategoria == sub,
                                    onClick = {
                                        subcategoria = sub
                                        if (tituloItem.isBlank()) {
                                            tituloItem = sub
                                        }
                                    },
                                    label = { Text("${emojiPorCategoria(categoria, sub)} $sub") }
                                )
                            }
                        }
                    }

                    // Descrição / Detalhes
                    OutlinedTextField(
                        value = tituloItem,
                        onValueChange = { tituloItem = it },
                        label = { Text("Descrição (ex: Cor, marca ou detalhes)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // 2. Local Encontrado no Mapa da Fatec
            Text(
                text = "Local encontrado",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            MapaInterativoFatec(
                pavimentoSelecionado = pavimento,
                onPavimentoChange = { novoPav ->
                    pavimento = novoPav
                    localMarcado = plantaPorPavimento[novoPav]?.firstOrNull()?.ladoEsquerdo ?: "Corredor 1"
                    coordX = null
                    coordY = null
                },
                localSelecionado = localMarcado,
                coordenadaX = coordX,
                coordenadaY = coordY,
                onLocalSelecionado = { local, x, y ->
                    localMarcado = local
                    coordX = x
                    coordY = y
                },
                somenteLeitura = false
            )

            // 3. Status de entrega à Coordenação
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { entregueCoordenacao = !entregueCoordenacao },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (entregueCoordenacao) Color(0xFFDCFCE7) else Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = entregueCoordenacao,
                        onCheckedChange = { entregueCoordenacao = it }
                    )
                    Column {
                        Text(
                            text = "Entregue à Coordenação",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (entregueCoordenacao)
                                "Disponível para retirada na Coordenação (1º Pavimento)"
                            else
                                "Item mantido no local indicado no mapa",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // 4. Botão Publicar
            Button(
                onClick = {
                    val fotoFinal = if (urlFotoCloudinary.isNotBlank()) {
                        urlFotoCloudinary
                    } else if (fotoCapturadaBitmap != null) {
                        val stream = ByteArrayOutputStream()
                        fotoCapturadaBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    } else ""

                    val tituloFinal = if (tituloItem.isNotBlank()) {
                        tituloItem
                    } else if (subcategoria.isNotBlank()) {
                        subcategoria
                    } else {
                        "$categoria encontrado(a)"
                    }

                    val novo = ItemPerdido(
                        id = System.currentTimeMillis().toString(),
                        titulo = tituloFinal,
                        categoria = categoria,
                        subcategoria = subcategoria,
                        pavimento = pavimento,
                        localEncontrado = localMarcado,
                        coordenadaCorredorX = coordX,
                        coordenadaCorredorY = coordY,
                        entregueNaCoordenacao = entregueCoordenacao,
                        fotoUrl = fotoFinal
                    )
                    onSalvarItem(novo)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Text(
                    text = "Publicar Item",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================
// TELA 4: DETALHES DO ITEM
// =========================================================================
@Composable
fun TelaDetalhesItem(
    item: ItemPerdido,
    onConfirmarRecuperacao: (ItemPerdido) -> Unit,
    onVoltar: () -> Unit,
    onAbrirConfiguracoes: () -> Unit
) {
    var mostrarDialogConfirmacao by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        HeaderPerdidos(
            mostrarBotaoVoltar = true,
            onVoltarClick = onVoltar,
            onConfigClick = onAbrirConfiguracoes
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card da Foto e Dados do Item
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Foto real do item ou emoji
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        ImagemCloudinaryOuEmoji(
                            fotoUrl = item.fotoUrl,
                            categoria = item.categoria,
                            subcategoria = item.subcategoria,
                            tamanhoEmoji = 64.sp
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val textoCategoria = if (item.subcategoria.isNotBlank() && item.subcategoria != item.titulo) {
                            "${item.categoria.uppercase()} • ${item.subcategoria}"
                        } else {
                            item.categoria.uppercase()
                        }
                        Text(
                            text = textoCategoria,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                        Text(
                            text = item.titulo,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "📍 ${item.pavimento.titulo} • ${item.localEncontrado}",
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Aviso de retirada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.entregueNaCoordenacao) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (item.entregueNaCoordenacao) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (item.entregueNaCoordenacao) Color(0xFF166534) else Color(0xFF92400E)
                    )
                    Column {
                        Text(
                            text = if (item.entregueNaCoordenacao)
                                "Entregue na Coordenação"
                            else
                                "Item no local indicado",
                            fontWeight = FontWeight.Bold,
                            color = if (item.entregueNaCoordenacao) Color(0xFF166534) else Color(0xFF92400E)
                        )
                        Text(
                            text = if (item.entregueNaCoordenacao)
                                "Retire na Sala de Coordenação (1º Pavimento)"
                            else
                                "Verifique o local demarcado no mapa abaixo",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Mapa de Localização
            Text(
                text = "Local indicado no mapa:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            MapaInterativoFatec(
                pavimentoSelecionado = item.pavimento,
                onPavimentoChange = {},
                localSelecionado = item.localEncontrado,
                coordenadaX = item.coordenadaCorredorX,
                coordenadaY = item.coordenadaCorredorY,
                onLocalSelecionado = { _, _, _ -> },
                somenteLeitura = true
            )

            Button(
                onClick = { mostrarDialogConfirmacao = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Text(
                    text = "Já recuperei este item",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (mostrarDialogConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogConfirmacao = false },
            title = { Text("Confirmar Recuperação", fontWeight = FontWeight.Bold) },
            text = {
                Text("Confirmar que recuperou \"${item.titulo}\"? O item sairá da lista para todos os alunos.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogConfirmacao = false
                        onConfirmarRecuperacao(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogConfirmacao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =========================================================================
// MODAL DE CONFIGURAÇÃO DE LEMBRETE (Dia, Semana ou Mês)
// =========================================================================
@Composable
fun ModalConfiguracaoLembrete(
    periodoAtual: String,
    onSelecionarPeriodo: (String) -> Unit,
    onFechar: () -> Unit
) {
    val opcoes = listOf("Dia", "Semana", "Mês")

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Text("Lembrete de Itens Perdidos", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Com qual frequência devemos perguntar se você perdeu algo na Fatec?",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
                opcoes.forEach { opcao ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelecionarPeriodo(opcao) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = periodoAtual == opcao,
                            onClick = { onSelecionarPeriodo(opcao) }
                        )
                        Text(
                            text = "A cada 1 $opcao",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFechar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Text("Salvar")
            }
        }
    )
}

fun emojiPorCategoria(categoria: String, subcategoria: String = ""): String = when (subcategoria) {
    "Carteirinha" -> "🪪"
    "Carteira" -> "👛"
    "Garrafa" -> "🧴"
    "Chaves" -> "🔑"
    "Óculos" -> "👓"
    "Guarda-chuva" -> "☂️"
    "Documentos" -> "📄"
    "Caderno / Livro" -> "📓"
    "Estojo" -> "✏️"
    "Caneta / Lápis" -> "🖊️"
    "Calculadora" -> "🔢"
    "Mochila / Pasta" -> "🎒"
    "Cabo HDMI" -> "📺"
    "Cabo / Carregador" -> "🔌"
    "Fone de Ouvido" -> "🎧"
    "Pen Drive" -> "💾"
    "Mouse / Teclado" -> "🖱️"
    "Casaco / Blusa" -> "🧥"
    "Boné / Gorro" -> "🧢"
    "Acessório" -> "⌚"
    else -> when (categoria) {
        "Item Pessoal" -> "🪪"
        "Material de Aula" -> "📚"
        "Eletrônicos" -> "🔌"
        "Vestuário" -> "🧥"
        else -> "📦"
    }
}