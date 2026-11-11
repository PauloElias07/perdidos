package com.example.perdidos

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSyncHelper {

    private val colecao get() = FirebaseFirestore.getInstance().collection("itens_perdidos")

    // 1. Quando um aluno cadastra um item, salva no Firebase para todos verem!
    fun salvarItemNaNuvem(item: ItemPerdido) {
        val dados = hashMapOf(
            "id" to item.id,
            "titulo" to item.titulo,
            "categoria" to item.categoria,
            "subcategoria" to item.subcategoria,
            "pavimento" to item.pavimento.name,
            "localEncontrado" to item.localEncontrado,
            "coordX" to item.coordenadaCorredorX,
            "coordY" to item.coordenadaCorredorY,
            "entregueNaCoordenacao" to item.entregueNaCoordenacao,
            "fotoUrl" to item.fotoUrl
        )
        colecao.document(item.id).set(dados)
    }

    // 2. Quando o dono clica em "Já recuperei", apaga do Firebase para diminuir a contagem de todos!
    fun removerItemDaNuvem(id: String) {
        colecao.document(id).delete()
    }

    // 3. Escuta a nuvem em tempo real e atualiza o SQLite do celular automaticamente!
    fun escutarAtualizacoesDaNuvem(
        bancoSqlite: BancoSqlitePerdidos,
        onListaAtualizada: (List<ItemPerdido>) -> Unit
    ) {
        colecao.addSnapshotListener { snapshot, erro ->
            if (erro != null || snapshot == null) return@addSnapshotListener

            // Se o Firebase ainda estiver vazio no 1º acesso, sobe os itens iniciais do SQLite para a nuvem
            if (snapshot.isEmpty) {
                bancoSqlite.listarItens().forEach { salvarItemNaNuvem(it) }
                return@addSnapshotListener
            }

            val itensNuvem = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val titulo = doc.getString("titulo") ?: return@mapNotNull null
                val categoria = doc.getString("categoria") ?: "Outros"
                val subcategoria = doc.getString("subcategoria") ?: ""
                val pavStr = doc.getString("pavimento") ?: "TERREO"
                val local = doc.getString("localEncontrado") ?: ""
                val coordX = doc.getDouble("coordX")?.toFloat()
                val coordY = doc.getDouble("coordY")?.toFloat()
                val entregue = doc.getBoolean("entregueNaCoordenacao") ?: true
                val fotoUrl = doc.getString("fotoUrl") ?: ""

                val pavimento = runCatching { PavimentoFatec.valueOf(pavStr) }
                    .getOrDefault(PavimentoFatec.TERREO)

                ItemPerdido(
                    id = id,
                    titulo = titulo,
                    categoria = categoria,
                    subcategoria = subcategoria,
                    pavimento = pavimento,
                    localEncontrado = local,
                    coordenadaCorredorX = coordX,
                    coordenadaCorredorY = coordY,
                    entregueNaCoordenacao = entregue,
                    fotoUrl = fotoUrl
                )
            }

            // Sincroniza o SQLite local com o que está na nuvem
            // Sincroniza o SQLite local usando a função da sua própria classe:
            bancoSqlite.atualizarTudoDaNuvem(itensNuvem)

            // Atualiza a tela Home e o Catálogo na mesma hora!
            onListaAtualizada(bancoSqlite.listarItens())

            // Atualiza a tela Home e o Catálogo na mesma hora!
            onListaAtualizada(bancoSqlite.listarItens())
        }
    }
}