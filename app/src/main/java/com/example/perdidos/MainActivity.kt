package com.example.perdidos

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

enum class TelaAtual {
    HOME,
    CATALOGO,
    CADASTRO,
    DETALHES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bancoSqlite = BancoSqlitePerdidos(this)

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
    val prefs = remember { context.getSharedPreferences("config_perdidos", Context.MODE_PRIVATE) }

    // Lista de itens carregada direto do SQLite!
    var itensPerdidos by remember { mutableStateOf(bancoSqlite.listarItens()) }
    LaunchedEffect(Unit) {
        FirebaseSyncHelper.escutarAtualizacoesDaNuvem(bancoSqlite) { listaSincronizada ->
            itensPerdidos = listaSincronizada
        }
    }

    // Controle de navegação entre as 4 telas do Figma
    var telaAtual by remember { mutableStateOf(TelaAtual.HOME) }
    var itemSelecionado by remember { mutableStateOf<ItemPerdido?>(null) }

    // Configuração do lembrete (Dia, Semana ou Mês)
    var periodoLembrete by remember {
        mutableStateOf(prefs.getString("periodo_lembrete", "Semana") ?: "Semana")
    }
    var mostrarModalConfig by remember { mutableStateOf(false) }

    // Permite usar o botão Voltar do próprio celular Android
    BackHandler(enabled = telaAtual != TelaAtual.HOME) {
        telaAtual = when (telaAtual) {
            TelaAtual.DETALHES -> TelaAtual.CATALOGO
            else -> TelaAtual.HOME
        }
    }

    when (telaAtual) {
        TelaAtual.HOME -> {
            TelaHomePerdidos(
                quantidadeItensPerdidos = itensPerdidos.size,
                periodoLembreteAtual = periodoLembrete,
                onIrParaCatalogo = {
                    if (itensPerdidos.isNotEmpty()) {
                        telaAtual = TelaAtual.CATALOGO
                    }
                },
                onIrParaCadastro = {
                    telaAtual = TelaAtual.CADASTRO
                },
                onAbrirConfiguracoes = { mostrarModalConfig = true }
            )
        }

        TelaAtual.CATALOGO -> {
            TelaCatalogoPerdidos(
                itens = itensPerdidos,
                onItemClick = { item ->
                    itemSelecionado = item
                    telaAtual = TelaAtual.DETALHES
                },
                onVoltar = { telaAtual = TelaAtual.HOME },
                onAbrirConfiguracoes = { mostrarModalConfig = true }
            )
        }

        TelaAtual.CADASTRO -> {
            TelaCadastrarItem(
                onSalvarItem = { novoItem ->
                    FirebaseSyncHelper.salvarItemNaNuvem(novoItem)
                    itensPerdidos = bancoSqlite.listarItens()
                    Toast.makeText(
                        context,
                        "Item cadastrado no SQLite! (${novoItem.pavimento.titulo} - ${novoItem.localEncontrado})",
                        Toast.LENGTH_SHORT
                    ).show()
                    telaAtual = TelaAtual.HOME
                },
                onVoltar = { telaAtual = TelaAtual.HOME },
                onAbrirConfiguracoes = { mostrarModalConfig = true }
            )
        }

        TelaAtual.DETALHES -> {
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
                        telaAtual = if (itensPerdidos.isNotEmpty()) TelaAtual.CATALOGO else TelaAtual.HOME
                    },
                    onVoltar = { telaAtual = TelaAtual.CATALOGO },
                    onAbrirConfiguracoes = { mostrarModalConfig = true }
                )
            }
        }
    }

    if (mostrarModalConfig) {
        ModalConfiguracaoLembrete(
            periodoAtual = periodoLembrete,
            onSelecionarPeriodo = { novoPeriodo ->
                periodoLembrete = novoPeriodo
                prefs.edit().putString("periodo_lembrete", novoPeriodo).apply()
            },
            onFechar = { mostrarModalConfig = false }
        )
    }
}