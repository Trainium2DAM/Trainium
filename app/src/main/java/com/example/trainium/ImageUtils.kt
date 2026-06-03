package com.example.trainium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Convierte una URI de imagen a una cadena Base64 comprimida.
 */
fun uriToBase64(uri: Uri, contentResolver: android.content.ContentResolver): String? {
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        // Comprimimos la imagen para que no ocupe demasiado en la DB (calidad 70)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Convierte una cadena Base64 de vuelta a un objeto Bitmap.
 */
fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}
