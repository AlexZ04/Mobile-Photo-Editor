package com.example.photoeditor.Translate

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

class Resize() {

    companion object {

        suspend fun resize(bitmap: Bitmap, coef: Double): Bitmap =
            withContext(Dispatchers.Default) {

                if (coef < 1 && bitmap.width > 32 && bitmap.height > 32) {
                    return@withContext trilinearFiltering(bitmap, coef.coerceIn(0.1, 10.0))
                } else if (coef > 1 && bitmap.width < 4096 && bitmap.height < 4096) {
                    return@withContext bilinearFiltering(bitmap, coef.coerceIn(0.1, 10.0))
                }

                bitmap
            }

        private suspend fun bilinearFiltering(bitmap: Bitmap, coef: Double): Bitmap = withContext(
            Dispatchers.Default
        ) {

            val newWidth = (bitmap.width * coef).toInt()
            val newHeight = (bitmap.height * coef).toInt()

            val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

            val ratioX = (bitmap.width - 1).toDouble() / (newWidth - 1)
            val ratioY = (bitmap.height - 1).toDouble() / (newHeight - 1)

            val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val chunkSize = bitmap.height / numCores

            coroutineScope {

                val tasks = mutableListOf<Deferred<Unit>>()

                for (startY in 0 until bitmap.height step chunkSize) {

                    val endY = (startY + chunkSize).coerceAtMost(bitmap.height)
                    val task = async {

                        for (i in 0 until bitmap.width) {
                            for (j in startY until endY) {

                                val leftX = floor(ratioX * i).toInt().coerceIn(0, bitmap.width - 1)
                                val rightX = ceil(ratioX * i).toInt().coerceIn(0, bitmap.width - 1)
                                val lowY = floor(ratioY * j).toInt().coerceIn(0, bitmap.height - 1)
                                val highY = ceil(ratioY * j).toInt().coerceIn(0, bitmap.height - 1)

                                val weightX = ratioX * i - leftX
                                val weightY = ratioY * j - lowY

                                fun mergeColor(col: Array<Float>): Float {

                                    return (col[0] * (1 - weightX) * (1 - weightY) +
                                            col[1] * weightX * (1 - weightY) +
                                            col[2] * weightY * (1 - weightX) +
                                            col[3] * weightX * weightY).toFloat()
                                }

                                if (0 <= leftX && leftX <= bitmap.width &&
                                    0 <= rightX && rightX <= bitmap.width &&
                                    0 <= lowY && lowY <= bitmap.height &&
                                    0 <= highY && highY <= bitmap.height
                                ) {


                                    val r = mergeColor(
                                        arrayOf(
                                            bitmap.getColor(leftX, lowY).red(),
                                            bitmap.getColor(rightX, lowY).red(),
                                            bitmap.getColor(leftX, highY).red(),
                                            bitmap.getColor(rightX, highY).red()
                                        )
                                    )

                                    val g = mergeColor(
                                        arrayOf(
                                            bitmap.getColor(leftX, lowY).green(),
                                            bitmap.getColor(rightX, lowY).green(),
                                            bitmap.getColor(leftX, highY).green(),
                                            bitmap.getColor(rightX, highY).green()
                                        )
                                    )

                                    val b = mergeColor(
                                        arrayOf(
                                            bitmap.getColor(leftX, lowY).blue(),
                                            bitmap.getColor(rightX, lowY).blue(),
                                            bitmap.getColor(leftX, highY).blue(),
                                            bitmap.getColor(rightX, highY).blue()
                                        )
                                    )

                                    if (i < newBitmap.width && j >= 0 && j < newBitmap.height) {
                                        newBitmap.setPixel(i, j, Color.rgb(r, g, b))
                                    }
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

        private suspend fun trilinearFiltering(bitmap: Bitmap, coef: Double): Bitmap =
            withContext(Dispatchers.Default) {

                var firstMipMapScale = 1.0
                while (firstMipMapScale > coef) {
                    firstMipMapScale /= 2
                }
                val secondMipMapScale = firstMipMapScale * 2

                val newWidth = (bitmap.width * coef).toInt()
                val newHeight = (bitmap.height * coef).toInt()

                val firstMM = bilinearFiltering(bitmap, firstMipMapScale)
                val secondMM = bilinearFiltering(bitmap, secondMipMapScale)

                val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                val chunkSize = bitmap.height / numCores

                coroutineScope {

                    val tasks = mutableListOf<Deferred<Unit>>()

                    for (startY in 0 until bitmap.height step chunkSize) {

                        val endY = (startY + chunkSize).coerceAtMost(bitmap.height)
                        val task = async {

                            for (i in 0 until bitmap.width) {
                                for (j in startY until endY) {


                                    val firstMipMapX =
                                        ((i / coef).toInt() * firstMipMapScale).toInt()
                                            .coerceIn(0, firstMM.width - 1)
                                    val firstMipMapY =
                                        ((j / coef).toInt() * firstMipMapScale).toInt()
                                            .coerceIn(0, firstMM.height - 1)

                                    val secondMipMapX =
                                        ((i / coef).toInt() * secondMipMapScale).toInt()
                                            .coerceIn(0, secondMM.width - 1)
                                    val secondMipMapY =
                                        ((j / coef).toInt() * secondMipMapScale).toInt()
                                            .coerceIn(0, secondMM.height - 1)

                                    val firstMMColor = firstMM.getColor(firstMipMapX, firstMipMapY)
                                    val secondMMColor =
                                        secondMM.getColor(secondMipMapX, secondMipMapY)

                                    val w =
                                        (coef - firstMipMapScale) / (secondMipMapScale - firstMipMapScale)

                                    val r =
                                        (firstMMColor.red() * (1 - w) + secondMMColor.red() * w).toFloat()
                                    val g =
                                        (firstMMColor.green() * (1 - w) + secondMMColor.green() * w).toFloat()
                                    val b =
                                        (firstMMColor.blue() * (1 - w) + secondMMColor.blue() * w).toFloat()

                                    if (i < newBitmap.width && j >= 0 && j < newBitmap.height) {
                                        newBitmap.setPixel(i, j, Color.rgb(r, g, b))
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