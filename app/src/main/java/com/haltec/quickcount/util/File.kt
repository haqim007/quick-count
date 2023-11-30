package com.haltec.quickcount.util

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import com.haltec.quickcount.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

private const val FILENAME_FORMAT = "dd_MMM_yyyy_HH_mm"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

//fun createFile(name: String, application: Application, mimeType: String = "jpg"): File {
//    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
//        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
//    }
//
//    val outputDirectory = if (
//        mediaDir != null && mediaDir.exists()
//    ) mediaDir else application.filesDir
//
//    return File(outputDirectory, name+"_$timeStamp.$mimeType")
//}

fun createFile(name: String, application: Application, mimeType: String = "jpg"): File {
    val appName = application.resources.getString(R.string.app_name)

    // External directory
    val externalMediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, appName).apply { mkdirs() }
    }

    // Internal directory
    val internalDir = File(application.filesDir, appName)

    // Choose the output directory based on the availability of the external media directory
    val outputDirectory = externalMediaDir ?: internalDir

    // Create a File object with the specified name, timestamp, MIME type, and extension
    return File(outputDirectory, name+"_$timeStamp.$mimeType")
}

fun createCacheFile(context: Context, filename: String, mimeType: String = "jpg"): File {
    return if (isExternalStorageWritable()){
        File(context.externalCacheDir, "$filename.$mimeType")
    }else{
        File.createTempFile("$filename.$mimeType", null, context.cacheDir)
    }
}

// Checks if a volume containing external storage is available
// for read and write.
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

// Checks if a volume containing external storage is available to at least read.
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}


fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

suspend fun downloadImageToCacheDir(context: Context, imageUrl: String, filename: String): File? = 
    withContext(Dispatchers.IO) {
    try {
        val connection = URL(imageUrl).openConnection()
        connection.connect()
        val input = connection.getInputStream()

        val output = createCacheFile(context, filename)
        val outputStream = FileOutputStream(output)

        val bitmap = BitmapFactory.decodeStream(input)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        outputStream.flush()
        outputStream.close()

        output
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}