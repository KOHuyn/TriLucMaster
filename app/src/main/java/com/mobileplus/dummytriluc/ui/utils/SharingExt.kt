package com.mobileplus.dummytriluc.ui.utils

/**
 * Created by KO Huyn on 06/07/2023.
 */
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadAndShareVideo(context: Activity, videoUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL(videoUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val fileName = Uri.parse(url.toString()).lastPathSegment

            val videoFile = File(context.cacheDir, fileName ?: "triluc")
            val outputStream = FileOutputStream(videoFile)

            val inputStream: InputStream = connection.inputStream

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                shareVideo(context, videoFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Video download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun shareVideo(context: Context, videoFile: File) {
    if (videoFile.exists()) {
        val videoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", videoFile)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, videoUri)
            type = "video/*"
        }
        val shareIntent = Intent.createChooser(intent, "Share")
        context.startActivity(shareIntent)
    } else {
        Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
    }
}
