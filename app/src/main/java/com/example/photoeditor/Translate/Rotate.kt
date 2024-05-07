package com.example.photoeditor.Translate

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.toColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Rotate() {
    companion object{

        private fun getNewPos(x: Int, y: Int, angle: Double): IntArray {

            val newX = (x * cos(angle) + y * sin(angle)).toInt()
            val newY = (y * cos(angle) - x * sin(angle)).toInt()

            return intArrayOf(newX, newY)
        }

        fun rotate(bitmap: Bitmap, angle: Double) : Bitmap {

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE

            val angleRad = PI * angle / 180

            for(i in 0..<bitmap.width * 2 step bitmap.width){
                for(j in 0..<bitmap.height * 2 step bitmap.height){

                    val newX = (i * cos(angleRad) - j * sin(angleRad)).toInt()
                    val newY = (i * sin(angleRad) + j * cos(angleRad)).toInt()
                    minX = min(minX, newX)
                    minY = min(minY, newY)
                    maxX = max(maxX, newX)
                    maxY = max(maxY, newY)
                }
            }

            val newBitmap = Bitmap.createBitmap(maxX - minX, maxY - minY, Bitmap.Config.ARGB_8888)
            for (x in 0..<newBitmap.getWidth()) {
                for (y in 0..<newBitmap.getHeight()) {

                    val(newX, newY) = getNewPos(x + minX, y + minY, angleRad)

                    val newColor = if(newX >= 0 && newX < bitmap.width
                        && newY >= 0 && newY < bitmap.height) bitmap.getColor(newX, newY) else Color.BLACK.toColor()

                    newBitmap.setPixel(x, y, newColor.toArgb())
                }
            }

            return newBitmap
        }
    }
}