package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ColorFilters() {
    companion object{
        fun negativeFilter(bitmap: Bitmap, fromX : Int = 0, fromY : Int = 0,
                           toX : Int = bitmap.width, toY: Int = bitmap.height) : Bitmap {

//            val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            for (x in fromX until toX) {
                for (y in fromY until toY) {
                    val color = bitmap.getPixel(x, y)

                    val r = Color.red(color)
                    val g = Color.green(color)
                    val b = Color.blue(color)

                    newBitmap.setPixel(x, y, Color.rgb(255 - r, 255 - g, 255 - b))
                }
            }

            return newBitmap
        }

        suspend fun blackWhiteFilter(bitmap: Bitmap, fromX: Int = 0, fromY: Int = 0,
                                     toX: Int = bitmap.width, toY: Int = bitmap.height): Bitmap = withContext(Dispatchers.Default) {

            val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val chunkSize = (toY - fromY) / numCores

            coroutineScope {

                val tasks = mutableListOf<Deferred<Unit>>()

                for (startY in fromY until toY step chunkSize) {

                    val endY = (startY + chunkSize).coerceAtMost(toY)
                    val task = async {

                        for (x in fromX until toX) {
                            for (y in startY until endY) {

                                val color = bitmap.getPixel(x, y)
                                val r = Color.red(color)
                                val g = Color.green(color)
                                val b = Color.blue(color)
                                val newColor = (r + g + b) / 3
                                newBitmap.setPixel(x, y, Color.rgb(newColor, newColor, newColor))
                            }
                        }
                    }
                    tasks.add(task)
                }

                tasks.awaitAll()
            }

            newBitmap
        }

        suspend fun mozaik(bitmap: Bitmap, pixels: Int, fromX : Int = 0, fromY : Int = 0,
                   toX : Int = bitmap.width, toY: Int = bitmap.height) : Bitmap = withContext(Dispatchers.Default) {

            val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            var color : Int

            var midR : Float
            var midG : Float
            var midB : Float

            var pixelsUsed = 0

            var x = fromX
            var y = 0

            var currentX = 0
            var currentY = 0

            coroutineScope {

                val task = async {
                    while (x < toX) {
                        y = fromY
                        while (y < toY) {
                            midR = 0F
                            midG = 0F
                            midB = 0F

                            pixelsUsed = 0

                            currentX = x
                            while (currentX < Math.min(x + pixels, toX)) {
                                currentY = y

                                while (currentY < Math.min(y + pixels, toY)) {
                                    color = bitmap.getPixel(currentX, currentY)

                                    pixelsUsed++

                                    midR += Color.red(color)
                                    midG += Color.green(color)
                                    midB += Color.blue(color)

                                    currentY++
                                }

                                currentX++
                            }

                            midR /= (pixelsUsed)
                            midG /= (pixelsUsed)
                            midB /= (pixelsUsed)

                            currentX = x
                            while (currentX < Math.min(x + pixels, toX)) {
                                currentY = y

                                while (currentY < Math.min(y + pixels, toY)) {
                                    newBitmap.setPixel(
                                        currentX, currentY, Color.rgb(
                                            midR.toInt(),
                                            midG.toInt(), midB.toInt()
                                        )
                                    )

                                    currentY++
                                }

                                currentX++
                            }

                            y += pixels
                        }

                        x += pixels
                    }
                }

                task.await()
            }

            newBitmap
        }
        suspend fun contrast(
            bitmap: Bitmap, correction: Int, fromX: Int = 0, fromY: Int = 0,
            toX: Int = bitmap.width, toY: Int = bitmap.height) : Bitmap  = withContext(Dispatchers.Default) {

            val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val colors = getColorsList(newBitmap, correction, fromX, fromY, toX, toY)

            val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val chunkSize = (toY - fromY) / numCores

            coroutineScope {

                val tasks = mutableListOf<Deferred<Unit>>()

                for (startY in fromY until toY step chunkSize) {

                    val endY = (startY + chunkSize).coerceAtMost(toY)
                    val task = async {

                        for (x in fromX until toX) {
                            for (y in startY until endY) {
                                val currentColor = bitmap.getPixel(x, y)

                                val curR = Color.red(currentColor)
                                val curG = Color.green(currentColor)
                                val curB = Color.blue(currentColor)

                                val newR = colors[curR]
                                val newG = colors[curG]
                                val newB = colors[curB]

                                newBitmap.setPixel(x, y, Color.rgb(newR, newG, newB))
                            }
                        }
                    }
                    tasks.add(task)
                }

                tasks.awaitAll()
            }

            newBitmap
        }

        fun getLAB(bitmap: Bitmap, fromX : Int = 0, fromY : Int = 0,
                   toX : Int = bitmap.width, toY: Int = bitmap.height): Float {
            var color: Int

            var valueR = 0
            var valueG = 0
            var valueB = 0

            var lAB = 0F

            for (x in fromX until toX) {
                for (y in fromY until toY) {
                    color = bitmap.getPixel(x, y)

                    valueR = Color.red(color)
                    valueG = Color.green(color)
                    valueB = Color.blue(color)

                    lAB += (valueR * 0.299 + valueG * 0.587 + valueB * 0.114).toInt()
                }
            }

            lAB /= (bitmap.width * bitmap.height)

            return lAB;
        }

        fun getColorsList(bitmap: Bitmap, correction: Int, fromX : Int = 0, fromY : Int = 0,
                          toX : Int = bitmap.width, toY: Int = bitmap.height): MutableList<Int> {
            val lAB = getLAB(bitmap, fromX, fromY, toX, toY).toInt()

            val k = 1.0 + correction / 100.0

            var delta : Int
            var newVal : Int

            val colors = mutableListOf <Int>()

            for (i in 0 until 256) {
                delta = i - lAB
                newVal = (lAB + k * delta).toInt()

                newVal = Math.max(0, Math.min(255, newVal))

                colors.add(newVal)

            }

            return colors
        }
    }
}
