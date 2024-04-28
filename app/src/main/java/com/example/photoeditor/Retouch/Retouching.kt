package com.example.photoeditor.Retouch

import android.graphics.Bitmap
import android.graphics.Color

class Retouching (_bitmap: Bitmap){
    var bitmap = _bitmap.copy(Bitmap.Config.ARGB_8888, true)
    private var redSum = 0
    private var greenSum = 0
    private var blueSum = 0
    private var pixelCount = 0
    fun getAverageColor(x: Int, y: Int, radius: Int) {
        for (i in (x - radius) until (x + radius)) {
            for (j in (y - radius) until (y + radius)) {
                if ((i - x) * (i - x) + (j - y) * (j - y) <= radius * radius &&
                    i >= 0 && i < bitmap.width && j >= 0 && j < bitmap.height) {
                    val color = bitmap.getPixel(i, j)

                    redSum += Color.red(color)
                    greenSum += Color.green(color)
                    blueSum += Color.blue(color)

                    pixelCount++
                }
            }
        }
    }

    fun startRetouching(x: Int, y: Int, radius: Int, strength: Int): Bitmap {
        getAverageColor(x,y,radius)
        val averageRed = redSum / pixelCount
        val averageGreen = greenSum / pixelCount
        val averageBlue = blueSum / pixelCount
        for (i in (x - radius) until (x + radius)) {
            for (j in (y - radius) until (y + radius)) {
                if ((i - x) * (i - x) + (j - y) * (j - y) <= radius * radius &&
                    i >= 0 && i < bitmap.width && j >= 0 && j < bitmap.height) {
                    val pixelColor = bitmap.getPixel(i, j)
                    val newRed = applyRetouching(Color.red(pixelColor), averageRed, strength)
                    val newGreen = applyRetouching(Color.green(pixelColor), averageGreen, strength)
                    val newBlue = applyRetouching(Color.blue(pixelColor), averageBlue, strength)
                    bitmap.setPixel(i, j, Color.rgb(newRed, newGreen, newBlue))
                }
            }
        }
        return bitmap
    }

    private fun applyRetouching(colorValue: Int, averageValue: Int, strength: Int): Int {
        val newColorValue = colorValue + (averageValue - colorValue) * strength / 100
        return newColorValue
    }
}