package com.example.perdidos


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object NotificacaoPerdidosHelper {

    private const val CANAL_ID = "canal_lembrete_perdidos"

    fun dispararLembretePerdidos(context: Context, periodo: String, qtdItens: Int) {
        // Verifica permissão no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val temPermissao = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!temPermissao) return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Lembretes de Achados e Perdidos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(canal)
        }

        val textoPeriodo = when (periodo) {
            "Dia" -> "no último dia"
            "Mês" -> "no último mês"
            else -> "na última semana"
        }

        val notificacao = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("PERDIDOS • Fatec Diadema")
            .setContentText("Você perdeu algo $textoPeriodo? Há $qtdItens itens esperando seus donos!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notificacao)
    }
}

/**
 * Se o item tiver uma foto salva no Cloudinary ou Base64 (fotoUrl), baixa e mostra a foto real!
 * Se não tiver foto, mostra o emoji da categoria/subcategoria.
 */
@Composable
fun ImagemCloudinaryOuEmoji(
    fotoUrl: String,
    categoria: String,
    subcategoria: String = "",
    tamanhoEmoji: TextUnit
) {
    var bitmapRemoto by remember(fotoUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(fotoUrl) {
        if (fotoUrl.isBlank()) {
            bitmapRemoto = null
            return@LaunchedEffect
        }
        if (fotoUrl.startsWith("http")) {
            bitmapRemoto = withContext(Dispatchers.IO) {
                runCatching {
                    URL(fotoUrl).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
        } else if (fotoUrl.startsWith("data:image")) {
            bitmapRemoto = withContext(Dispatchers.IO) {
                runCatching {
                    val base64Data = fotoUrl.substringAfter("base64,")
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }.getOrNull()
            }
        }
    }

    if (bitmapRemoto != null) {
        Image(
            bitmap = bitmapRemoto!!.asImageBitmap(),
            contentDescription = "Foto do item",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Text(
            text = emojiPorCategoria(categoria, subcategoria),
            fontSize = tamanhoEmoji
        )
    }
}