
package com.jamia.madinatulilm.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageStorageManager {

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
