package com.example.photoeditor.Affine

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.toColor
import com.example.photoeditor.Translate.Rotate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class AffineTransformations {

    companion object {

        suspend fun transform(
            bitmap: Bitmap,
            firstPoints: List<Array<Float>>,
            secondPoints: List<Array<Float>>
        ): Bitmap = withContext(
            Dispatchers.Default
        ) {

            val (x1, y1) = firstPoints[0]
            val (x2, y2) = firstPoints[1]
            val (x3, y3) = firstPoints[2]
            val (newX1, newY1) = secondPoints[0]
            val (newX2, newY2) = secondPoints[1]
            val (newX3, newY3) = secondPoints[2]

            val b =
                if ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2) != 0.0f) ((newX1 - newX2) * (x2 - x3) - (newX2 - newX3) * (x1 - x2)) /
                        ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2)) else 0.0f
            val a = if (x1 - x2 != 0.0f) (newX1 - newX2 - b * (y1 - y2)) / (x1 - x2) else 1.0f
            val e = newX1 - a * x1 - b * y1

            val d =
                if ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2) != 0.0f) ((newY1 - newY2) * (x2 - x3) - (newY2 - newY3) * (x1 - x2)) /
                        ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2)) else 1.0f
            val c = if (x1 - x2 != 0.0f) (newY1 - newY2 - d * (y1 - y2)) / (x1 - x2) else 0.0f
            val f = newY1 - c * x1 - d * y1

            val newBitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val chunkSize = bitmap.height / numCores

            coroutineScope {

                val tasks = mutableListOf<Deferred<Unit>>()

                for (startY in 0 until bitmap.height step chunkSize) {

                    val endY = (startY + chunkSize).coerceAtMost(bitmap.height)
                    val task = async {

                        for (x in 0 until bitmap.width) {
                            for (y in startY until endY) {

                                val newX =
                                    ((d * x - b * y + b * f - e * d) / (a * d - b * c)).toInt()
                                val newY = ((y - c * newX - f) / d).toInt()

                                if (newX >= 0 && newX < bitmap.width
                                    && newY >= 0 && newY < bitmap.height
                                ) {

                                    val newColor = bitmap.getColor(newX, newY)
                                    newBitmap.setPixel(x, y, newColor.toArgb())
                                }
                            }
                        }
                    }
                    tasks.add(task)
                }

                tasks.awaitAll()
            }

            newBitmap
        }
    }
}