package com.example.photoeditor.Translate

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.toColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Rotate() {
    companion object{
        suspend fun rotate(bitmap: Bitmap, angle: Double): Bitmap = withContext(Dispatchers.Default) {
            val cosAngle = cos(PI * angle / 180)
            val sinAngle = sin(PI * angle / 180)

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE

            // Calculate the new bounds
            for (i in 0 until bitmap.width) {
                for (j in 0 until bitmap.height) {
                    val newX = (i * cosAngle - j * sinAngle).toInt()
                    val newY = (i * sinAngle + j * cosAngle).toInt()

                    minX = min(minX, newX)
                    minY = min(minY, newY)
                    maxX = max(maxX, newX)
                    maxY = max(maxY, newY)
                }
            }

            val newWidth = maxX - minX + 1
            val newHeight = maxY - minY + 1
            val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

            coroutineScope {

                val jobs = List(newBitmap.width) { x ->

                    async(Dispatchers.Default) {

                        for (y in 0 until newBitmap.height) {
                            val newX = ((x + minX) * cosAngle + (y + minY) * sinAngle).toInt()
                            val newY = ((y + minY) * cosAngle - (x + minX) * sinAngle).toInt()

                            if (newX in 0 until bitmap.width && newY in 0 until bitmap.height) {
                                val color = bitmap.getPixel(newX, newY)
                                newBitmap.setPixel(x, y, color)
                            }
                        }
                    }
                }
                jobs.awaitAll()
            }

            return@withContext newBitmap
        }
    }
}