package com.example.photoeditor.neuron

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

class FaceDetector () {
    fun processImage(faceCascade: CascadeClassifier, newImageBitmap: Bitmap) : Bitmap {
        //val filter = ColorFilters(newImageBitmap)
        val mat = Mat()
        Utils.bitmapToMat(newImageBitmap, mat)
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val faces = MatOfRect()
        faceCascade.detectMultiScale(gray, faces)

        val faceArray = faces.toArray()
        for (face in faceArray) {
            Imgproc.rectangle(mat, face.tl(), face.br(), Scalar(255.0, 0.0, 0.0), 5)
            //filter.negativeFilter(face.tl().x.toInt(), face.tl().y.toInt(), face.br().x.toInt(), face.br().y.toInt())
        }


        Utils.matToBitmap(mat, newImageBitmap)
        //val resultBitmap = filter.bitmap.copy(Bitmap.Config.ARGB_8888, true)
        return newImageBitmap
    }
}