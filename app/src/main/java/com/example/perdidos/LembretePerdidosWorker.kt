package com.example.perdidos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker do WorkManager para executar a checagem e emissão periódica
 * de notificações de itens perdidos, mesmo com o app fechado.
 */
class LembretePerdidosWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val bancoSqlite = BancoSqlitePerdidos(applicationContext)
            val qtdItens = bancoSqlite.listarItens().size
            val periodo = PreferenciasPerdidosRepository
                .obterPeriodoLembrete(applicationContext)
                .first()

            NotificacaoPerdidosHelper.dispararLembretePerdidos(
                context = applicationContext,
                periodo = periodo,
                qtdItens = qtdItens
            )
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}

/**
 * Utilitário responsável pelo agendamento periódico das notificações com WorkManager.
 */
object NotificacaoAgendador {

    private const val WORK_NAME = "lembrete_perdidos_work"

    /**
     * Agenda notificações periódicas no sistema usando o WorkManager,
     * respeitando a escolha do usuário gravada no DataStore ("Dia", "Semana", "Mês").
     */
    fun agendarLembrete(context: Context, periodo: String) {
        val (duracao, unidade) = when (periodo) {
            "Dia" -> Pair(1L, TimeUnit.DAYS)
            "Mês" -> Pair(30L, TimeUnit.DAYS)
            else -> Pair(7L, TimeUnit.DAYS) // "Semana"
        }

        val request = PeriodicWorkRequestBuilder<LembretePerdidosWorker>(duracao, unidade)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Dispara uma notificação imediatamente (útil para testes em sala de aula).
     */
    fun dispararImediatamente(context: Context, periodo: String, qtdItens: Int) {
        NotificacaoPerdidosHelper.dispararLembretePerdidos(context, periodo, qtdItens)
    }
}
