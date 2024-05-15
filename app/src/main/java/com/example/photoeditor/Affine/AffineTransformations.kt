package com.example.photoeditor.Affine

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.toColor
import com.example.photoeditor.Translate.Rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class AffineTransformations {

    companion object{

        fun transform(bitmap: Bitmap, firstPoints: List<Array<Float>>, secondPoints: List<Array<Float>>) : Bitmap{

            val (x1, y1) = firstPoints[0]
            val (x2, y2) = firstPoints[1]
            val (x3, y3) = firstPoints[2]
            val (newX1, newY1) = secondPoints[0]
            val (newX2, newY2) = secondPoints[1]
            val (newX3, newY3) = secondPoints[2]

//            Log.d("HUH", x1.toString())
//            Log.d("HUH", y1.toString())
//            Log.d("HUH", x2.toString())
//            Log.d("HUH", y2.toString())
//            Log.d("HUH", x3.toString())
//            Log.d("HUH", y3.toString())
//            Log.d("HUH", newX1.toString())
//            Log.d("HUH", newY1.toString())
//            Log.d("HUH", newX2.toString())
//            Log.d("HUH", newY2.toString())
//            Log.d("HUH", newX3.toString())
//            Log.d("HUH", newY3.toString())
//
//            val (x1, y1) = arrayOf(0.0,0.0)
//            val (x2, y2) = arrayOf(0.0,10.0)
//            val (x3, y3) = arrayOf(10.0,0.0)
//            val (newX1, newY1) = arrayOf(0.0,0.0)
//            val (newX2, newY2) = arrayOf(0.0,10.0)
//            val (newX3, newY3) = arrayOf(10.0,0.0)

            val b = if ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2) != 0.0f) ((newX1 - newX2) * (x2 - x3) - (newX2 - newX3) * (x1 - x2)) /
                    ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2)) else 0.0f
            val a = if (x1 - x2 != 0.0f) (newX1 - newX2 - b * (y1 - y2)) / (x1 - x2) else 1.0f
            val e = newX1 - a * x1 - b * y1

            val d = if ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2) != 0.0f) ((newY1 - newY2) * (x2 - x3) - (newY2 - newY3) * (x1 - x2)) /
                    ((y1 - y2) * (x2 - x3) - (y2 - y3) * (x1 - x2)) else 1.0f
            val c = if(x1 - x2 != 0.0f) (newY1 - newY2 - d * (y1 - y2)) / (x1 - x2) else 0.0f
            val f = newY1 - c * x1 - d * y1

            val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            for (x in 0..<newBitmap.getWidth()) {
                for (y in 0..<newBitmap.getHeight()) {

                    val newX = ((d * x - b * y + b * f - e * d) / (a * d - b * c)).toInt()
                    val newY = ((y - c * newX - f) / d).toInt()
                    val newColor = if(newX >= 0 && newX < bitmap.width
                        && newY >= 0 && newY < bitmap.height) bitmap.getColor(newX, newY) else Color.BLACK.toColor()

                    newBitmap.setPixel(x, y, newColor.toArgb())
                }
            }

            Log.d("HUH", a.toString())
            Log.d("HUH", b.toString())
            Log.d("HUH", c.toString())
            Log.d("HUH", d.toString())
            Log.d("HUH", e.toString())
            Log.d("HUH", f.toString())
            return newBitmap
        }
    }
}