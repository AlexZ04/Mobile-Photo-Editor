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

    fun getLAB(): Float {
        var color: Int

        var valueR = 0
        var valueG = 0
        var valueB = 0

        var lAB = 0F

        for (x in 0 until this.bitmap.width) {
            for (y in 0 until this.bitmap.height) {
                color = this.bitmap.getPixel(x, y)

                valueR = Color.red(color)
                valueG = Color.green(color)
                valueB = Color.blue(color)

                lAB += (valueR * 0.299 + valueG * 0.587 + valueB * 0.114).toInt()
            }
        }

        lAB /= (this.bitmap.width * this.bitmap.height)

        return lAB;
    }

    fun getColorsList(correction: Int): MutableList<Int> {
        val lAB = getLAB().toInt()

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

    fun contrast(correction: Int) {

        var colors = getColorsList(correction)

        var currentColor : Int

        var curR : Int
        var curG : Int
        var curB : Int

        var newR : Int
        var newG : Int
        var newB : Int

        for (x in 0 until this.bitmap.width) {
            for (y in 0 until this.bitmap.height) {
                currentColor = this.bitmap.getPixel(x, y)

                curR = Color.red(currentColor)
                curG = Color.green(currentColor)
                curB = Color.blue(currentColor)

                newR = colors[curR]
                newG = colors[curG]
                newB = colors[curB]

                this.bitmap.setPixel(x, y, Color.rgb(newR, newG, newB))
            }
        }

    }
}
