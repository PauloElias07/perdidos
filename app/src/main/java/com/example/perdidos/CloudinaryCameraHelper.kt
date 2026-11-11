package com.example.perdidos


import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object CloudinaryService {

    // ⚠️ COLOQUE AQUI O SEU CLOUD NAME E O UPLOAD PRESET DO CLOUDINARY:
    private const val CLOUD_NAME = "jw91nl1g"
    private const val UPLOAD_PRESET = "perdidos_fatec"

    /**
     * Recebe a foto tirada pela Câmera (Bitmap), comprime em JPEG
     * e envia para a API REST do Cloudinary, devolvendo a URL pública da imagem!
     */
    suspend fun enviarFotoParaCloudinary(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Converte o Bitmap da câmera para Base64 JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytesImagem = outputStream.toByteArray()
            val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(bytesImagem, Base64.NO_WRAP)

            // 2. Monta a requisição para a API do Cloudinary
            val url = URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            val conexao = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            val postData = "file=${URLEncoder.encode(base64Image, "UTF-8")}" +
                    "&upload_preset=${URLEncoder.encode(UPLOAD_PRESET, "UTF-8")}"

            OutputStreamWriter(conexao.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            // 3. Lê a resposta e devolve o link "secure_url" da foto salva na nuvem!
            val codigoResposta = conexao.responseCode
            if (codigoResposta in 200..299) {
                val respostaJson = conexao.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(respostaJson)
                val urlFotoSalva = jsonObj.getString("secure_url")
                Result.success(urlFotoSalva)
            } else {
                val erroMsg = conexao.errorStream?.bufferedReader()?.use { it.readText() } ?: "Erro HTTP $codigoResposta"
                Result.failure(Exception("Erro Cloudinary: $erroMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}