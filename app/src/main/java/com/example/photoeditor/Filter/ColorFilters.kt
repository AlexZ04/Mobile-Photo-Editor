package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.abs

class ColorFilters(_bitmap: Bitmap) {
//    val bitmap: Bitmap = _bitmap
    var bitmap = _bitmap.copy(Bitmap.Config.ARGB_8888, true)
    fun negativeFilter() {

        for (x in 0 until this.bitmap.getWidth()) {
            for (y in 0 until this.bitmap.getHeight()) {
                val color = this.bitmap.getPixel(x, y)

                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                this.bitmap.setPixel(x, y, Color.rgb(255 - r, 255 - g, 255 - b))
            }
        }
    }

    fun blackWhiteFilter() {

        for (x in 0 until this.bitmap.getWidth()) {
            for (y in 0 until this.bitmap.getHeight()) {
                val color = this.bitmap.getPixel(x, y)

                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                val newColor = (r + g + b) / 3

                this.bitmap.setPixel(x, y, Color.rgb(newColor, newColor, newColor))
            }
        }
    }

    fun mozaik(pixels: Int) {

        var color : Int

        var midR : Float
        var midG : Float
        var midB : Float

        var pixelsUsed = 0

        var x = 0
        var y = 0

        var currentX = 0
        var currentY = 0

        while (x < this.bitmap.width) {
            y = 0
            while (y < this.bitmap.height) {
                midR = 0F
                midG = 0F
                midB = 0F

                pixelsUsed = 0

                currentX = x
                while (currentX < Math.min(x + pixels, this.bitmap.width)) {
                    currentY = y

                    while (currentY < Math.min(y + pixels, this.bitmap.height)) {
                        color = this.bitmap.getPixel(currentX, currentY)

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
                while (currentX < Math.min(x + pixels, this.bitmap.width)) {
                    currentY = y

                    while (currentY < Math.min(y + pixels, this.bitmap.height)) {
                        this.bitmap.setPixel(currentX, currentY, Color.rgb(midR.toInt(),
                            midG.toInt(), midB.toInt()))

                        currentY++
                    }

                    currentX++
                }

                y += pixels
            }

            x += pixels
        }

    }

    fun balanceWhite() { // как будто он не так должен работать

        var r = 0
        var g = 0
        var b = 0

        for (x in 0 until this.bitmap.getWidth()) {
            for (y in 0 until this.bitmap.getHeight()) {
                val color = this.bitmap.getPixel(x, y)

                r += Color.red(color)
                g += Color.green(color)
                b += Color.blue(color)
            }
        }

        r /= (this.bitmap.getWidth() * this.bitmap.getHeight())
        g /= (this.bitmap.getWidth() * this.bitmap.getHeight())
        b /= (this.bitmap.getWidth() * this.bitmap.getHeight())

        println(r)

        val newColor = (r + g + b) / 3

        for (x in 0 until this.bitmap.getWidth()) {
            for (y in 0 until this.bitmap.getHeight()) {
                val color = this.bitmap.getPixel(x, y)

                val newR = abs(Color.red(color) - newColor) / 2
                val newG = abs(Color.green(color) - newColor) / 2
                val newB = abs(Color.blue(color) - newColor) / 2

                this.bitmap.setPixel(x, y, Color.rgb(newR, newG, newB))
            }
        }
    }
}
