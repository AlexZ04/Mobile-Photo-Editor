package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

class UnsharpMask(_bitmap: Bitmap) {
    var bitmap = _bitmap.copy(Bitmap.Config.ARGB_8888, true)

    fun generateGaussianFilter(size : Int, sigma: Double): Array<DoubleArray> {
        val filter = Array(size) { DoubleArray(size) }

        val s = 2 * sigma * sigma
        var sum = 0.0

        val exponentValues = mutableListOf <Double>()
        var r : Double

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
                idx ++
            }
        }

        for (i in 0 until size) {
            for (j in 0 until size) {
                filter[i][j] /= sum
            }
        }

        return filter

    }

    fun getGaussianBitmap(): Bitmap? {
        val gaussianBitmap = this.bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val size = 29
        val radius = size / 2

        val filter = generateGaussianFilter(size, 7.2)

        var newR : Double
        var newG : Double
        var newB : Double

        var newX : Int
        var newY : Int

        var color : Int

        println(this.bitmap.height * this.bitmap.width)

        for (i in 0 until this.bitmap.width) {
            for (j in 0 until this.bitmap.height) {
                newR = 0.0
                newG = 0.0
                newB = 0.0

                for (x in -radius until radius) {
                    for (y in -radius until radius) {
                        newX = i + x
                        newY = j + y
                        if (newX >= 0 && newX < this.bitmap.width && newY >= 0 && newY <
                            this.bitmap.height) {
                            color = this.bitmap.getPixel(newX, newY)

                            newR += Color.red(color) * filter[x + radius][y + radius]
                            newG += Color.green(color) * filter[x + radius][y + radius]
                            newB += Color.blue(color) * filter[x + radius][y + radius]
                        }
                    }
                }

                gaussianBitmap.setPixel(i, j, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
            }

        }

        //applyGaussianFilterRGB(image, 2400, 2400, generateGaussianFilter(29, 7.2))
        return gaussianBitmap
    }
    fun unsharpMaskAlg(coef: Double) {
//        var gaussianBitmap = generateGaussianFilter(29, 7.2)
        var gaussianBitmap = getGaussianBitmap()

        var color: Int
        var gaussianColor = 0

        var newR : Double
        var newG : Double
        var newB : Double

        for (x in 0 until this.bitmap.width) {

            for (y in 0 until this.bitmap.height) {
                color = this.bitmap.getPixel(x, y)
                if (gaussianBitmap != null) {
                    gaussianColor = gaussianBitmap.getPixel(x, y)
                }

                newR = Color.red(color) + coef * (Color.red(color) - Color.red(gaussianColor))
                newG = Color.green(color) + coef * (Color.green(color) - Color.green(gaussianColor))
                newB = Color.blue(color) + coef * (Color.blue(color) - Color.blue(gaussianColor))

                newR = Math.min(255.0, Math.max(0.0, newR))
                newG = Math.min(255.0, Math.max(0.0, newG))
                newB = Math.min(255.0, Math.max(0.0, newB))

                this.bitmap.setPixel(x, y, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
            }
        }
    }
}