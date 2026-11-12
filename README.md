# 📱 e-PERDIDOS • Fatec Diadema

> **Aplicativo Android nativo para gestão inteligente e colaborativa de Achados e Perdidos no campus da Fatec Diadema.**

---

## 📌 Sobre o Projeto

O **e-PERDIDOS** é uma solução mobile desenvolvida em **Kotlin** e **Jetpack Compose** para resolver o problema recorrente de itens esquecidos ou perdidos pelos alunos e colaboradores no campus da **Fatec Diadema**.

O app substitui murais físicos ou avisos dispersos por uma plataforma integrada onde quem encontra um objeto pode fotografá-lo, categorizá-lo e indicar **exatamente onde o encontrou no mapa interativo dos 3 pavimentos da faculdade** (Térreo, 1º e 2º pavimentos), informando ainda se o item foi entregue na Sala de Coordenação ou se permaneceu no local.

---

## 🎯 Requisitos Acadêmicos Atendidos

| Requisito | Tecnologia / Implementação | Descrição |
| :--- | :--- | :--- |
| **Navegação entre Telas** | **Navigation Compose (`NavHost` / `NavController`)** | Navegação declarativa fluida entre 5 telas principais (`HOME`, `CATALOGO`, `CADASTRO`, `DETALHES` e `CONFIGURACOES`), com suporte nativo ao botão físico/gestual de voltar do Android via pilha de navegação (`popBackStack`). |
| **DataStore (Preferências)** | **Jetpack DataStore Preferences** | Armazenamento reativo e assíncrono das preferências do usuário (`periodo_lembrete`: Dia, Semana ou Mês), exposto por Kotlin `Flow` e gerenciado em `PreferenciasPerdidosRepository`. |
| **SQLite (Persistência Local)** | **SQLiteOpenHelper (`perdidos.db`)** | Banco relacional local completo com tabela `itens_perdidos`, operações de CRUD (`listarItens`, `inserirItem`, `removerItemRecuperado`, `atualizarTudoDaNuvem`) e carga inicial de demonstração. |
| **API REST (Serviço Externo)** | **HTTP Client + Cloudinary REST API** | Consumo direto da API REST pública do Cloudinary (`POST /image/upload`) para upload de fotos de itens em Base64 e parsing de resposta JSON para recuperação da `secure_url`. |
| **Notificações Locais (Agendamento)** | **Android WorkManager + NotificationCompat** | Agendamento de rotinas periódicas com `LembretePerdidosWorker` respeitando o intervalo salvo no DataStore, canais de notificação (`NotificationChannel`), permissão `POST_NOTIFICATIONS` (Android 13+) e botão para disparo de teste imediato. |

---

## 🚀 Funcionalidades Principais

1. **Dashboard Home**:
   - Contador dinâmico em tempo real de itens perdidos aguardando resgate.
   - Acesso rápido ao catálogo de itens e ao fluxo de cadastro de novo item.
   - Indicador do lembrete periódico ativo com atalho para reconfiguração.

2. **Catálogo Interativo de Itens**:
   - Visualização em grade responsiva de 2 colunas.
   - Carregamento de fotos reais via URL remota ou Base64, com fallback para emojis contextuais.
   - Busca textual instantânea por título, categoria, subcategoria e local/sala.
   - Filtros horizontais dinâmicos por categoria (Item Pessoal, Material de Aula, Eletrônicos, Vestuário, etc.).
   - Badge visual indicando se o objeto já está seguro na Coordenação.

3. **Cadastro com Câmera e Mapa Interativo**:
   - Abertura da câmera nativa do dispositivo com `ActivityResultContracts.TakePicturePreview`.
   - **Pré-visualização da foto com conferência visual** e **Modal de Imagem Completa** em tela cheia (ao tocar na foto ou no atalho "Ver completa"), com opção de "Tirar outra".
   - Seleção de categorias genéricas e subcategorias específicas (ex: Carteirinha, Cabo HDMI, Garrafa).
   - **Planta interativa da Fatec Diadema**: seleção por toque do andar (Térreo, 1º ou 2º) e marcação da sala ou do ponto exato no corredor com coordenadas normalizadas.
   - Checkbox informando se o item foi entregue na Coordenação.

4. **Detalhes do Item e Resgate**:
   - Imagem do objeto com **Modal de Visualização Completa** em tela cheia (toque sobre a imagem para abrir a foto inteira sem cortes com `ContentScale.Fit`).
   - Mapa em modo somente leitura destacando onde o item foi achado.
   - Instruções claras de retirada (Sala da Coordenação ou local original).
   - Ação de confirmação de recuperação ("Já recuperei"), que remove o item da nuvem e do banco local, atualizando a contagem para todos os usuários.

5. **Página de Configuração de Lembretes**:
   - Tela completa e dedicada de configurações acessível através do ícone de engrenagem nas barras superiores.
   - Seleção interativa da frequência dos lembretes ("A cada 1 Dia", "A cada 1 Semana", "A cada 1 Mês") salva no Jetpack DataStore.
   - Botão para disparo de teste imediato de notificação com WorkManager.
   - Card informativo explicando o funcionamento da persistência reativa e do agendamento em segundo plano.

6. **Sincronização em Nuvem (Firebase Cloud Firestore)**:
   - Sincronização automática em tempo real entre diferentes dispositivos via `addSnapshotListener`, garantindo que os dados do SQLite local se mantenham sempre atualizados com a nuvem.

---

## 🏗️ Arquitetura e Estrutura de Arquivos

```text
app/src/main/java/com/example/perdidos/
│
├── MainActivity.kt               # Ponto de entrada, rotas e NavHost do Navigation Compose
├── DadosFatec.kt                 # Modelos de dados (ItemPerdido, PavimentoFatec), mapa das salas e categorias
├── TelasPerdidos.kt              # Telas da UI (Home, Catálogo, Cadastro, Detalhes e Configuração de Lembrete)
├── ComponenteMapaFatec.kt        # Componente da planta baixa interativa da Fatec Diadema
│
├── BancoSqlitePerdidos.kt        # Persistência local em SQLite (SQLiteOpenHelper)
├── DataStoreHelper.kt            # Armazenamento de preferências com Jetpack DataStore
├── LembretePerdidosWorker.kt     # Agendador e Worker em segundo plano com WorkManager
├── NotificacaoHelper.kt          # Emissão de notificações locais e renderizador de imagens
│
├── CloudinaryCameraHelper.kt     # Consumo da API REST do Cloudinary para upload de imagens
└── FireBaseSyncHelper.kt         # Sincronização em tempo real via Cloud Firestore
```

---

## 🛠️ Tecnologias e Bibliotecas

- **Linguagem**: Kotlin 2.2+
- **Interface Declarativa**: Jetpack Compose com Material Design 3
- **Navegação entre Telas**: Jetpack Navigation Compose (`androidx.navigation:navigation-compose`)
- **Persistência Local**: Android SQLite (`android.database.sqlite`)
- **Preferências**: AndroidX DataStore Preferences (`androidx.datastore:datastore-preferences`)
- **Agendamento em Background**: AndroidX WorkManager (`androidx.work:work-runtime-ktx`)
- **Câmera**: Activity Result Contracts (`TakePicturePreview`, `RequestPermission`)
- **Backend em Nuvem**: Firebase Cloud Firestore (`com.google.firebase:firebase-firestore`)
- **Serviço REST**: Cloudinary Image Upload API (`HttpURLConnection` + JSON)
- **Target SDK / Min SDK**: Android 15 (API 37) / Android 7.0 (API 24)

---

## ⚙️ Como Executar o Projeto

1. Clone o repositório ou abra a pasta do projeto no **Android Studio**.
2. Aguarde a sincronização do Gradle (**Gradle Sync**).
3. Conecte um dispositivo físico via USB com Depuração ativada ou inicie um emulador Android.
4. Execute o app clicando em **Run 'app'** (`Shift + F10`).
