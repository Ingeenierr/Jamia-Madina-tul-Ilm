package com.jamia.madinatulilm.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.jamia.madinatulilm.R
import java.io.File

class ComposeFileProvider : FileProvider(
    R.xml.file_paths
) {
    companion object {
        fun getUriForFile(context: Context, file: File): Uri {
            return getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        fun getIntentForFile(context: Context, file: File): Intent {
            val uri = getUriForFile(context, file)
            return Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.ms-excel")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
