package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

class UnsharpMask() {
    companion object {

        fun generateGaussianFilter(size: Int, sigma: Double): Array<DoubleArray> {
            val filter = Array(size) { DoubleArray(size) }

            val s = 2 * sigma * sigma
            var sum = 0.0

            val exponentValues = mutableListOf<Double>()
            var r: Double

            for (x in -size / 2 until size / 2) {

                for (y in -size / 2 until size / 2) {
                    r = sqrt((x * x + y * y).toDouble())
                    exponentValues.add(exp(-(r * r) / s))
                }
            }

            var idx = 0
            for (x in -size / 2 until size / 2) {

                for (y in -size / 2 until size / 2) {
                    filter[x + size / 2][y + size / 2] = exponentValues[idx] / (PI * s)
                    sum += filter[x + size / 2][y + size / 2]
                    idx++
                }
            }

            for (i in 0 until size) {
                for (j in 0 until size) {
                    filter[i][j] /= sum
                }
            }

            return filter

        }

        fun getGaussianBitmap(bitmap: Bitmap): Bitmap? {
            val gaussianBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val size = 29
            val radius = size / 2

            val filter = generateGaussianFilter(size, 7.2)

            var newR: Double
            var newG: Double
            var newB: Double

            var newX: Int
            var newY: Int

            var color: Int

            println(bitmap.height * bitmap.width)

            for (i in 0 until bitmap.width) {
                for (j in 0 until bitmap.height) {
                    newR = 0.0
                    newG = 0.0
                    newB = 0.0

                    for (x in -radius until radius) {
                        for (y in -radius until radius) {
                            newX = i + x
                            newY = j + y
                            if (newX >= 0 && newX < bitmap.width && newY >= 0 && newY <
                                bitmap.height
                            ) {
                                color = bitmap.getPixel(newX, newY)

                                newR += Color.red(color) * filter[x + radius][y + radius]
                                newG += Color.green(color) * filter[x + radius][y + radius]
                                newB += Color.blue(color) * filter[x + radius][y + radius]
                            }
                        }
                    }

                    gaussianBitmap.setPixel(
                        i,
                        j,
                        Color.rgb(newR.toInt(), newG.toInt(), newB.toInt())
                    )
                }

            }

            //applyGaussianFilterRGB(image, 2400, 2400, generateGaussianFilter(29, 7.2))
            return gaussianBitmap
        }

        suspend fun unsharpMaskAlg(bitmap: Bitmap, coef: Double): Bitmap =
            withContext(Dispatchers.Default) {

                val newBitmap =
                    Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//        var gaussianBitmap = generateGaussianFilter(29, 7.2)
                val gaussianBitmap = getGaussianBitmap(bitmap)

                val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                val chunkSize = bitmap.height / numCores

                coroutineScope {

                    val tasks = mutableListOf<Deferred<Unit>>()

                    for (startY in 0 until bitmap.height step chunkSize) {

                        val endY = (startY + chunkSize).coerceAtMost(bitmap.height)
                        val task = async {

                            for (x in 0 until bitmap.width) {
                                for (y in startY until endY) {

                                    val color = bitmap.getPixel(x, y)
                                    var gaussianColor = 0
                                    if (gaussianBitmap != null) {
                                        gaussianColor = gaussianBitmap.getPixel(x, y)
                                    }

                                    var newR =
                                        Color.red(color) + coef * (Color.red(color) - Color.red(
                                            gaussianColor
                                        ))
                                    var newG =
                                        Color.green(color) + coef * (Color.green(color) - Color.green(
                                            gaussianColor
                                        ))
                                    var newB =
                                        Color.blue(color) + coef * (Color.blue(color) - Color.blue(
                                            gaussianColor
                                        ))

                                    newR = Math.min(255.0, Math.max(0.0, newR))
                                    newG = Math.min(255.0, Math.max(0.0, newG))
                                    newB = Math.min(255.0, Math.max(0.0, newB))

                                    newBitmap.setPixel(
                                        x,
                                        y,
                                        Color.rgb(newR.toInt(), newG.toInt(), newB.toInt())
                                    )
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