package com.example.perdidos

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

enum class TelaRota {
    HOME,
    CATALOGO,
    CADASTRO,
    DETALHES,
    CONFIGURACOES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bancoSqlite = BancoSqlitePerdidos(this)

        // Inicializa o agendamento de lembrete com WorkManager
        NotificacaoAgendador.agendarLembrete(this, "Semana")

        setContent {
            MaterialTheme {
                Perdidos(bancoSqlite = bancoSqlite)
            }
        }
    }
}

@Composable
fun Perdidos(bancoSqlite: BancoSqlitePerdidos) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Permissão de notificações no Android 13+ (API 33+)
    val permissaoNotificacaoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissaoNotificacaoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Preferência reativa do período de lembrete lida diretamente do Jetpack DataStore!
    val periodoLembrete by PreferenciasPerdidosRepository
        .obterPeriodoLembrete(context)
        .collectAsState(initial = "Semana")

    // Lista de itens carregada direto do SQLite!
    var itensPerdidos by remember { mutableStateOf(bancoSqlite.listarItens()) }
    LaunchedEffect(Unit) {
        FirebaseSyncHelper.escutarAtualizacoesDaNuvem(bancoSqlite) { listaSincronizada ->
            itensPerdidos = listaSincronizada
        }
    }

    var itemSelecionado by remember { mutableStateOf<ItemPerdido?>(null) }

    NavHost(
        navController = navController,
        startDestination = TelaRota.HOME.name
    ) {
        composable(TelaRota.HOME.name) {
            TelaHomePerdidos(
                quantidadeItensPerdidos = itensPerdidos.size,
                periodoLembreteAtual = periodoLembrete,
                onIrParaCatalogo = {
                    if (itensPerdidos.isNotEmpty()) {
                        navController.navigate(TelaRota.CATALOGO.name)
                    }
                },
                onIrParaCadastro = {
                    navController.navigate(TelaRota.CADASTRO.name)
                },
                onAbrirConfiguracoes = {
                    navController.navigate(TelaRota.CONFIGURACOES.name)
                }
            )
        }

        composable(TelaRota.CATALOGO.name) {
            TelaCatalogoPerdidos(
                itens = itensPerdidos,
                onItemClick = { item ->
                    itemSelecionado = item
                    navController.navigate(TelaRota.DETALHES.name)
                },
                onVoltar = { navController.popBackStack() },
                onAbrirConfiguracoes = {
                    navController.navigate(TelaRota.CONFIGURACOES.name)
                }
            )
        }

        composable(TelaRota.CADASTRO.name) {
            TelaCadastrarItem(
                onSalvarItem = { novoItem ->
                    FirebaseSyncHelper.salvarItemNaNuvem(novoItem)
                    itensPerdidos = bancoSqlite.listarItens()
                    Toast.makeText(
                        context,
                        "Item cadastrado no SQLite! (${novoItem.pavimento.titulo} - ${novoItem.localEncontrado})",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate(TelaRota.HOME.name) {
                        popUpTo(TelaRota.HOME.name) { inclusive = true }
                    }
                },
                onVoltar = { navController.popBackStack() },
                onAbrirConfiguracoes = {
                    navController.navigate(TelaRota.CONFIGURACOES.name)
                }
            )
        }

        composable(TelaRota.DETALHES.name) {
            val item = itemSelecionado
            if (item != null) {
                TelaDetalhesItem(
                    item = item,
                    onConfirmarRecuperacao = { itemRecuperado ->
                        FirebaseSyncHelper.removerItemDaNuvem(itemRecuperado.id)
                        itensPerdidos = bancoSqlite.listarItens()
                        Toast.makeText(
                            context,
                            "Que ótimo que recuperou! Contagem atualizada.",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (itensPerdidos.isNotEmpty()) {
                            navController.navigate(TelaRota.CATALOGO.name) {
                                popUpTo(TelaRota.HOME.name)
                            }
                        } else {
                            navController.navigate(TelaRota.HOME.name) {
                                popUpTo(TelaRota.HOME.name) { inclusive = true }
                            }
                        }
                    },
                    onVoltar = { navController.popBackStack() },
                    onAbrirConfiguracoes = {
                        navController.navigate(TelaRota.CONFIGURACOES.name)
                    }
                )
            }
        }

        composable(TelaRota.CONFIGURACOES.name) {
            TelaConfiguracaoLembrete(
                periodoAtual = periodoLembrete,
                onSelecionarPeriodo = { novoPeriodo ->
                    coroutineScope.launch {
                        PreferenciasPerdidosRepository.salvarPeriodoLembrete(context, novoPeriodo)
                        NotificacaoAgendador.agendarLembrete(context, novoPeriodo)
                        Toast.makeText(
                            context,
                            "Lembrete atualizado no DataStore e agendado!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onTestarNotificacao = {
                    NotificacaoAgendador.dispararImediatamente(
                        context = context,
                        periodo = periodoLembrete,
                        qtdItens = itensPerdidos.size
                    )
                    Toast.makeText(context, "Notificação emitida!", Toast.LENGTH_SHORT).show()
                },
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}
