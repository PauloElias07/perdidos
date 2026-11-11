package com.example.perdidos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensão Context para instanciar o Preferences DataStore (Singleton oficial)
val Context.dataStorePerdidos: DataStore<Preferences> by preferencesDataStore(name = "config_perdidos")

object PreferenciasPerdidosRepository {

    private val CHAVE_PERIODO_LEMBRETE = stringPreferencesKey("periodo_lembrete")

    /**
     * Retorna um Flow contínuo com a preferência do período de lembrete
     * ("Dia", "Semana" ou "Mês"), com valor padrão "Semana".
     */
    fun obterPeriodoLembrete(context: Context): Flow<String> {
        return context.dataStorePerdidos.data.map { preferences ->
            preferences[CHAVE_PERIODO_LEMBRETE] ?: "Semana"
        }
    }

    /**
     * Salva de forma assíncrona o novo período escolhido no DataStore.
     */
    suspend fun salvarPeriodoLembrete(context: Context, novoPeriodo: String) {
        context.dataStorePerdidos.edit { preferences ->
            preferences[CHAVE_PERIODO_LEMBRETE] = novoPeriodo
        }
    }
}
