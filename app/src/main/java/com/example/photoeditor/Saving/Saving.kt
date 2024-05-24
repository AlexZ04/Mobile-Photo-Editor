package com.example.photoeditor.Saving

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream

class Saving {
    companion object {

        fun saveBitmapToUri(context: Context, bitmap: Bitmap, uri: Uri) {
            var fos: OutputStream? = null

            context.contentResolver?.also { resolver ->
                fos = resolver.openOutputStream(uri)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        }

        fun createImageUri(context: Context): Uri? {

            val filename = "${System.currentTimeMillis()}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            return context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        }
    }
}
