package com.example.photoeditor.Filter

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

class UnsharpMask(_bitmap: Bitmap) {
    var bitmap = _bitmap.copy(Bitmap.Config.ARGB_8888, true)

    fun getGaussianMatrix(): Bitmap? {
        var gaussianBitmap = this.bitmap.copy(Bitmap.Config.ARGB_8888, true)

        return gaussianBitmap
    }
    fun unsharpMaskAlg(coef: Int) {
        var gaussianBitmap = getGaussianMatrix()

        var color: Int
        var gaussianColor = 0

        var newR : Int
        var newG : Int
        var newB : Int

        for (x in 0 until this.bitmap.width) {

            for (y in 0 until this.bitmap.height) {
                color = this.bitmap.getPixel(x, y)
                if (gaussianBitmap != null) {
                    gaussianColor = gaussianBitmap.getPixel(x, y)
                }

                newR = Color.red(color) + coef * (Color.red(color) - Color.red(gaussianColor))
                newG = Color.green(color) + coef * (Color.green(color) - Color.green(gaussianColor))
                newB = Color.blue(color) + coef * (Color.blue(color) - Color.blue(gaussianColor))

                this.bitmap.setPixel(x, y, Color.rgb(newR, newG, newB))
            }
        }
    }
}