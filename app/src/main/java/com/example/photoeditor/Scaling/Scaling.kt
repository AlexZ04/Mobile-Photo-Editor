package com.example.photoeditor.Translate

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.ceil
import kotlin.math.floor

class Resize() {

    companion object{

        fun resize(bitmap: Bitmap, coef: Double) : Bitmap{

            if(coef < 1){
                return trilinearFiltering(bitmap, coef)
            }
            else{
                return bilinearFiltering(bitmap, coef)
            }
        }

         private fun bilinearFiltering(bitmap: Bitmap, coef: Double) : Bitmap {

            val newWidth = (bitmap.width * coef).toInt()
            val newHeight = (bitmap.height * coef).toInt()

            val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

            val ratioX = (bitmap.width - 1).toDouble() / (newWidth - 1)
            val ratioY = (bitmap.height - 1).toDouble() / (newHeight - 1)

            for(i in 0..<newWidth){
                for(j in 0..<newHeight){

                    val leftX = floor(ratioX * i).toInt()
                    val rightX = ceil(ratioX * i).toInt()
                    val lowY = floor(ratioY * j).toInt()
                    val highY = ceil(ratioY * j).toInt()

                    val weightX = ratioX * i - leftX
                    val weightY = ratioY * j - lowY

                    fun mergeColor(col: Array<Float>): Float{

                        return (col[0] * (1 - weightX) * (1 - weightY) +
                                col[1] * weightX * (1 - weightY) +
                                col[2] * weightY * (1 - weightX) +
                                col[3] * weightX * weightY).toFloat()
                    }

                    if(0 <= leftX && leftX <= bitmap.width &&
                        0 <= rightX && rightX <= bitmap.width &&
                        0 <= lowY && lowY <= bitmap.height &&
                        0 <= highY && highY <= bitmap.height) {


                        val r = mergeColor(arrayOf(bitmap.getColor(leftX, lowY).red(),
                            bitmap.getColor(rightX, lowY).red(),
                            bitmap.getColor(leftX, highY).red(),
                            bitmap.getColor(rightX, highY).red()))

                        val g = mergeColor(arrayOf(bitmap.getColor(leftX, lowY).green(),
                            bitmap.getColor(rightX, lowY).green(),
                            bitmap.getColor(leftX, highY).green(),
                            bitmap.getColor(rightX, highY).green()))

                        val b = mergeColor(arrayOf(bitmap.getColor(leftX, lowY).blue(),
                            bitmap.getColor(rightX, lowY).blue(),
                            bitmap.getColor(leftX, highY).blue(),
                            bitmap.getColor(rightX, highY).blue()))


                        newBitmap.setPixel(i, j, Color.rgb(r, g, b))
                    }
                }
            }

            return newBitmap
        }

        private fun trilinearFiltering(bitmap: Bitmap, coef: Double) : Bitmap {

            var firstMipMapScale = 1.0
            while(firstMipMapScale > coef){
                firstMipMapScale /= 2
            }
            val secondMipMapScale = firstMipMapScale * 2

            val newWidth = (bitmap.width * coef).toInt()
            val newHeight = (bitmap.height * coef).toInt()

            val firstMM = bilinearFiltering(bitmap, firstMipMapScale)
            val secondMM = bilinearFiltering(bitmap, secondMipMapScale)

            val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

            for(i in 0..<newWidth){
                for(j in 0..<newHeight){

                    val firstMipMapX = ((i / coef).toInt() * firstMipMapScale).toInt()
                    val firstMipMapY = ((j / coef).toInt() * firstMipMapScale).toInt()

                    val secondMipMapX = ((i / coef).toInt() * secondMipMapScale).toInt()
                    val secondMipMapY = ((j / coef).toInt() * secondMipMapScale).toInt()

                    val firstMMColor = firstMM.getColor(firstMipMapX, firstMipMapY)
                    val secondMMColor = secondMM.getColor(secondMipMapX, secondMipMapY)

                    val w = (coef - firstMipMapScale) / (secondMipMapScale - firstMipMapScale)

                    val r = (firstMMColor.red() * (1 - w) + secondMMColor.red() * w).toFloat()
                    val g = (firstMMColor.green() * (1 - w) + secondMMColor.green() * w).toFloat()
                    val b = (firstMMColor.blue() * (1 - w) + secondMMColor.blue() * w).toFloat()

                    newBitmap.setPixel(i, j, Color.rgb(r, g, b))
                }
            }

            return newBitmap
        }
    }
}