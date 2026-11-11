package com.example.perdidos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoSqlitePerdidos(context: Context) : SQLiteOpenHelper(
    context,
    NOME_BANCO,
    null,
    VERSAO_BANCO
) {

    companion object {
        private const val NOME_BANCO = "perdidos.db"
        private const val VERSAO_BANCO = 3

        private const val TABELA_ITENS = "itens_perdidos"
        private const val COL_ID = "id"
        private const val COL_TITULO = "titulo"
        private const val COL_CATEGORIA = "categoria"
        private const val COL_SUBCATEGORIA = "subcategoria"
        private const val COL_PAVIMENTO = "pavimento"
        private const val COL_LOCAL = "local_encontrado"
        private const val COL_COORD_X = "coordenada_corredor_x"
        private const val COL_COORD_Y = "coordenada_corredor_y"
        private const val COL_ENTREGUE = "entregue_na_coordenacao"
        private const val COL_FOTO_URL = "foto_url"
        private const val COL_DATA = "data_encontrado"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCriarTabela = """
            CREATE TABLE $TABELA_ITENS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITULO TEXT NOT NULL,
                $COL_CATEGORIA TEXT NOT NULL,
                $COL_SUBCATEGORIA TEXT NOT NULL DEFAULT '',
                $COL_PAVIMENTO TEXT NOT NULL,
                $COL_LOCAL TEXT NOT NULL,
                $COL_COORD_X REAL,
                $COL_COORD_Y REAL,
                $COL_ENTREGUE INTEGER NOT NULL,
                $COL_FOTO_URL TEXT NOT NULL,
                $COL_DATA TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlCriarTabela)

        // Inicializa com os itens de exemplo definidos em DadosFatec.kt
        itensIniciaisExemplo.forEach { item ->
            val values = ContentValues().apply {
                put(COL_ID, item.id)
                put(COL_TITULO, item.titulo)
                put(COL_CATEGORIA, item.categoria)
                put(COL_SUBCATEGORIA, item.subcategoria)
                put(COL_PAVIMENTO, item.pavimento.name)
                put(COL_LOCAL, item.localEncontrado)
                item.coordenadaCorredorX?.let { put(COL_COORD_X, it) } ?: putNull(COL_COORD_X)
                item.coordenadaCorredorY?.let { put(COL_COORD_Y, it) } ?: putNull(COL_COORD_Y)
                put(COL_ENTREGUE, if (item.entregueNaCoordenacao) 1 else 0)
                put(COL_FOTO_URL, item.fotoUrl)
                put(COL_DATA, item.dataEncontrado)
            }
            db.insert(TABELA_ITENS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABELA_ITENS ADD COLUMN $COL_SUBCATEGORIA TEXT NOT NULL DEFAULT ''")
            } catch (_: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABELA_ITENS")
                onCreate(db)
            }
        }
    }

    fun listarItens(): List<ItemPerdido> {
        val lista = mutableListOf<ItemPerdido>()
        val db = readableDatabase
        val cursor = db.query(
            TABELA_ITENS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_ID)
            val tituloIndex = it.getColumnIndexOrThrow(COL_TITULO)
            val categoriaIndex = it.getColumnIndexOrThrow(COL_CATEGORIA)
            val subcatIndex = it.getColumnIndex(COL_SUBCATEGORIA)
            val pavimentoIndex = it.getColumnIndexOrThrow(COL_PAVIMENTO)
            val localIndex = it.getColumnIndexOrThrow(COL_LOCAL)
            val coordXIndex = it.getColumnIndexOrThrow(COL_COORD_X)
            val coordYIndex = it.getColumnIndexOrThrow(COL_COORD_Y)
            val entregueIndex = it.getColumnIndexOrThrow(COL_ENTREGUE)
            val fotoUrlIndex = it.getColumnIndexOrThrow(COL_FOTO_URL)
            val dataIndex = it.getColumnIndexOrThrow(COL_DATA)

            while (it.moveToNext()) {
                val pavimentoStr = it.getString(pavimentoIndex)
                val pavimento = try {
                    PavimentoFatec.valueOf(pavimentoStr)
                } catch (_: IllegalArgumentException) {
                    PavimentoFatec.TERREO
                }

                val coordX = if (it.isNull(coordXIndex)) null else it.getFloat(coordXIndex)
                val coordY = if (it.isNull(coordYIndex)) null else it.getFloat(coordYIndex)
                val subcategoria = if (subcatIndex != -1 && !it.isNull(subcatIndex)) it.getString(subcatIndex) else ""

                val item = ItemPerdido(
                    id = it.getString(idIndex),
                    titulo = it.getString(tituloIndex),
                    categoria = it.getString(categoriaIndex),
                    subcategoria = subcategoria,
                    pavimento = pavimento,
                    localEncontrado = it.getString(localIndex),
                    coordenadaCorredorX = coordX,
                    coordenadaCorredorY = coordY,
                    entregueNaCoordenacao = it.getInt(entregueIndex) == 1,
                    fotoUrl = it.getString(fotoUrlIndex) ?: "",
                    dataEncontrado = it.getString(dataIndex) ?: "Hoje"
                )
                lista.add(item)
            }
        }
        return lista
    }

    fun atualizarTudoDaNuvem(itensDaNuvem: List<ItemPerdido>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABELA_ITENS, null, null)
            itensDaNuvem.forEach { item ->
                inserirItem(item)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun inserirItem(novoItem: ItemPerdido) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, novoItem.id)
            put(COL_TITULO, novoItem.titulo)
            put(COL_CATEGORIA, novoItem.categoria)
            put(COL_SUBCATEGORIA, novoItem.subcategoria)
            put(COL_PAVIMENTO, novoItem.pavimento.name)
            put(COL_LOCAL, novoItem.localEncontrado)
            novoItem.coordenadaCorredorX?.let { put(COL_COORD_X, it) } ?: putNull(COL_COORD_X)
            novoItem.coordenadaCorredorY?.let { put(COL_COORD_Y, it) } ?: putNull(COL_COORD_Y)
            put(COL_ENTREGUE, if (novoItem.entregueNaCoordenacao) 1 else 0)
            put(COL_FOTO_URL, novoItem.fotoUrl)
            put(COL_DATA, novoItem.dataEncontrado)
        }
        db.insertWithOnConflict(TABELA_ITENS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun removerItemRecuperado(id: String) {
        val db = writableDatabase
        db.delete(TABELA_ITENS, "$COL_ID = ?", arrayOf(id))
    }
}
