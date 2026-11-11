package com.example.perdidos

enum class PavimentoFatec(val titulo: String) {
    TERREO("Térreo"),
    PRIMEIRO("1º Pavimento"),
    SEGUNDO("2º Pavimento")
}

// Representa cada linha do seu esquema: Lado Esquerdo | Corredor | Lado Direito
data class LinhaMapaFatec(
    val ladoEsquerdo: String,
    val trechoCorredor: String,
    val ladoDireito: String
)

// Representa um Item Perdido (Pronto para o SQLite e Firebase)
data class ItemPerdido(
    val id: String,
    val titulo: String,
    val categoria: String, // "Item Pessoal", "Material de Aula", "Eletrônicos", "Vestuário", "Outros"
    val subcategoria: String = "", // "Carteirinha", "Carteira", "Garrafa", "Cabo HDMI", etc.
    val pavimento: PavimentoFatec,
    val localEncontrado: String, // Nome da sala ou "Corredor"
    val coordenadaCorredorX: Float? = null, // Posição X (0.0 a 1.0) se marcou no corredor
    val coordenadaCorredorY: Float? = null, // Posição Y (0.0 a 1.0) se marcou no corredor
    val entregueNaCoordenacao: Boolean = true,
    val fotoUrl: String = "",
    val dataEncontrado: String = "Hoje"
)

// Categorias genéricas e suas subcategorias
val mapaCategoriasSubcategorias: Map<String, List<String>> = mapOf(
    "Item Pessoal" to listOf("Carteirinha", "Carteira", "Garrafa", "Chaves", "Óculos", "Guarda-chuva", "Documentos", "Outro"),
    "Material de Aula" to listOf("Caderno / Livro", "Estojo", "Caneta / Lápis", "Calculadora", "Mochila / Pasta", "Outro"),
    "Eletrônicos" to listOf("Cabo HDMI", "Cabo / Carregador", "Fone de Ouvido", "Pen Drive", "Mouse / Teclado", "Outro"),
    "Vestuário" to listOf("Casaco / Blusa", "Boné / Gorro", "Acessório", "Outro"),
    "Outros" to listOf("Geral")
)

// Lista de categorias para filtros do Catálogo
val categoriasPerdidos: List<String> = listOf("Todos") + mapaCategoriasSubcategorias.keys.toList()

// O MAPA EXATO QUE VOCÊ MONTOU NO BLOCO DE NOTAS PARA OS 3 PAVIMENTOS:
val plantaPorPavimento: Map<PavimentoFatec, List<LinhaMapaFatec>> = mapOf(
    PavimentoFatec.TERREO to listOf(
        LinhaMapaFatec("Lab Química I", "Entrada / Corredor 1", "Lab Maquiagem"),
        LinhaMapaFatec("Escada (Topo)", "Corredor 2", "Lab Semi-sólidos"),
        LinhaMapaFatec("Elevador", "Corredor 3", "Lab Sólidos"),
        LinhaMapaFatec("Aquário (PCs)", "Corredor 4", "Refeitório Alunos"),
        LinhaMapaFatec("Área Comum 1", "Corredor 5", "Refeitório Professores"),
        LinhaMapaFatec("Sec. Serv. Acadêmicos", "Corredor 6", "Banheiro Masc (PCD/Comum)"),
        LinhaMapaFatec("Área Comum 2", "Corredor 7", "Banheiro Fem (PCD/Comum)"),
        LinhaMapaFatec("Escada (Fundo)", "Corredor 8", "Sec. Serv. Administrativos"),
        LinhaMapaFatec("Biblioteca / Auditório", "Corredor 9", "")
    ),
    PavimentoFatec.PRIMEIRO to listOf(
        LinhaMapaFatec("Lab Química II", "Corredor 1", "Lab Análise Instrumental"),
        LinhaMapaFatec("Escada (Topo)", "Corredor 2", ""),
        LinhaMapaFatec("Elevador", "Corredor 3", "Sala dos Professores"),
        LinhaMapaFatec("Sala 1", "Corredor 4", "Direção"),
        LinhaMapaFatec("Sala 3", "Corredor 5", "Sala de Coordenação"),
        LinhaMapaFatec("Escada (Meio)", "Corredor 6", "Banheiro Masc (PCD/Comum)"),
        LinhaMapaFatec("", "Corredor 7", "Banheiro Fem (PCD/Comum)"),
        LinhaMapaFatec("Sala 5", "Corredor 8", "Sala 4"),
        LinhaMapaFatec("Lab Ciências Biológicas", "Corredor 9", "Lab Desenvolvimento")
    ),
    PavimentoFatec.SEGUNDO to listOf(
        LinhaMapaFatec("Lab Info. II", "Corredor 1", "Lab Info. I"),
        LinhaMapaFatec("Escada (Topo)", "Corredor 2", "Almoxarifado"),
        LinhaMapaFatec("Elevador", "Corredor 3", "Sala 6"),
        LinhaMapaFatec("Sala 7", "Corredor 4", "Sala 8"),
        LinhaMapaFatec("Sala 9", "Corredor 5", "Banheiro Masc (PCD/Comum)"),
        LinhaMapaFatec("Escada (Meio)", "Corredor 6", "Banheiro Fem (PCD/Comum)"),
        LinhaMapaFatec("", "Corredor 7", "Sala 10"),
        LinhaMapaFatec("Sala 11", "Corredor 8", ""),
        LinhaMapaFatec("Sala 13", "Corredor 9", "Sala 12"),
        LinhaMapaFatec("Sala 15", "Corredor 10", "Sala Maker")
    )
)

// Alguns itens iniciais com as novas categorias e subcategorias
val itensIniciaisExemplo = listOf(
    ItemPerdido(
        id = "1",
        titulo = "Guarda-chuva Preto",
        categoria = "Item Pessoal",
        subcategoria = "Guarda-chuva",
        pavimento = PavimentoFatec.TERREO,
        localEncontrado = "Aquário (PCs)",
        entregueNaCoordenacao = true
    ),
    ItemPerdido(
        id = "2",
        titulo = "Caderno Capa Dura Azul",
        categoria = "Material de Aula",
        subcategoria = "Caderno / Livro",
        pavimento = PavimentoFatec.PRIMEIRO,
        localEncontrado = "Lab Química II",
        entregueNaCoordenacao = true
    ),
    ItemPerdido(
        id = "3",
        titulo = "Garrafa Térmica Verde",
        categoria = "Item Pessoal",
        subcategoria = "Garrafa",
        pavimento = PavimentoFatec.TERREO,
        localEncontrado = "Refeitório Alunos",
        entregueNaCoordenacao = false
    ),
    ItemPerdido(
        id = "4",
        titulo = "Cabo USB-C Branco",
        categoria = "Eletrônicos",
        subcategoria = "Cabo / Carregador",
        pavimento = PavimentoFatec.SEGUNDO,
        localEncontrado = "Lab Info. I",
        entregueNaCoordenacao = true
    ),
    ItemPerdido(
        id = "5",
        titulo = "Cabo HDMI Preto 2m",
        categoria = "Eletrônicos",
        subcategoria = "Cabo HDMI",
        pavimento = PavimentoFatec.SEGUNDO,
        localEncontrado = "Lab Info. II",
        entregueNaCoordenacao = true
    ),
    ItemPerdido(
        id = "6",
        titulo = "Carteirinha Fatec",
        categoria = "Item Pessoal",
        subcategoria = "Carteirinha",
        pavimento = PavimentoFatec.PRIMEIRO,
        localEncontrado = "Direção",
        entregueNaCoordenacao = true
    ),
    ItemPerdido(
        id = "7",
        titulo = "Óculos de Grau Preto",
        categoria = "Item Pessoal",
        subcategoria = "Óculos",
        pavimento = PavimentoFatec.PRIMEIRO,
        localEncontrado = "Corredor 4",
        coordenadaCorredorX = 0.5f,
        coordenadaCorredorY = 0.4f,
        entregueNaCoordenacao = true
    )
)