package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
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