package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color

class ColorFilters(_bitmap: Bitmap) {
//    val bitmap: Bitmap = _bitmap
    var bitmap = _bitmap.copy(Bitmap.Config.ARGB_8888, true)
    fun negativeFilter() {
        println(this.bitmap.width)

        for (x in 0 until this.bitmap.getWidth()) {
            for (y in 0 until this.bitmap.getHeight()) {
                var r = this.bitmap.getColor(x, y).red()
                var g = this.bitmap.getColor(x, y).green()
                var b = this.bitmap.getColor(x, y).blue()

                this.bitmap.setPixel(x, y, Color.rgb(1 - r, 1 - g, 1 - b))
            }
        }
    }
}
