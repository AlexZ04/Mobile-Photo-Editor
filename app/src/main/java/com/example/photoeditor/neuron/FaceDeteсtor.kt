package com.example.photoeditor.neuron

import android.graphics.Bitmap
import com.example.photoeditor.Filter.ColorFilters
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetector () {
    fun processImage(faceCascade: CascadeClassifier, newImageBitmap: Bitmap, stateOfDetector: Int) : Bitmap {
        var mutableBitmap = newImageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val mat = Mat()
        Utils.bitmapToMat(mutableBitmap, mat)
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val faces = MatOfRect()
        faceCascade.detectMultiScale(gray, faces)

        val faceArray = faces.toArray()
        for (face in faceArray) {
            when (stateOfDetector) {
                0 -> {
                    Imgproc.rectangle(mat, face.tl(), face.br(), Scalar(255.0, 0.0, 0.0), 5)
                }
                1 -> {
                    mutableBitmap = ColorFilters.negativeFilter(mutableBitmap, face.tl().x.toInt(), face.tl().y.toInt(), face.br().x.toInt(), face.br().y.toInt())
                }
                2 -> {
                    mutableBitmap = ColorFilters.blackWhiteFilter(mutableBitmap, face.tl().x.toInt(), face.tl().y.toInt(), face.br().x.toInt(), face.br().y.toInt())
                }
                3 -> {
                    mutableBitmap = ColorFilters.mozaik(mutableBitmap, 8, face.tl().x.toInt(), face.tl().y.toInt(), face.br().x.toInt(), face.br().y.toInt())
                }
            }

        }
        if (stateOfDetector == 0){
            Utils.matToBitmap(mat, mutableBitmap)
        }

        return mutableBitmap
    }
}