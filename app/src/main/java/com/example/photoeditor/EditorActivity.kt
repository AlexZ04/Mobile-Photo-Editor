package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.Filter.ColorFilters
import com.example.photoeditor.Retouch.Retouching
import kotlin.math.roundToInt

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier
import java.io.File


class EditorActivity : AppCompatActivity() {

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button
    private lateinit var colorFilterButton: Button
    private lateinit var faceDetectorButton: Button
    private lateinit var faceCascade: CascadeClassifier

    private lateinit var newImageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OpenCVLoader.initDebug()
        setContentView(R.layout.activity_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainImage = findViewById(R.id.mainImage)
        choosePickButton = findViewById(R.id.choosePickButton)
        colorFilterButton = findViewById(R.id.colorFilterButton)
        faceDetectorButton = findViewById(R.id.faceDetectorButton)

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, intent.data)
        newImageBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        mainImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        colorFilterButton.setOnClickListener{
            val objectImage = ColorFilters(newImageBitmap)
            objectImage.negativeFilter()
            newImageBitmap = objectImage.bitmap.copy(Bitmap.Config.ARGB_8888, true)

            mainImage.setImageBitmap(newImageBitmap)
        }
        val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_default)
        val file = File(cacheDir, "haarcascade_frontalface_default.xml")
        inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        faceCascade = CascadeClassifier(file.absolutePath)

        faceDetectorButton.setOnClickListener{
            processImage()
        }

        mainImage.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                val imageView = v as ImageView
                val drawable = imageView.drawable
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight

                val imageWidth = imageView.width
                val imageHeight = imageView.height
                val x = (event.x / imageWidth * intrinsicWidth).toInt()
                val y = (event.y / imageHeight * intrinsicHeight).toInt()
                val retouching = Retouching(newImageBitmap)
                newImageBitmap = retouching.startRetouching(x, y, 500, 100)
                mainImage.setImageBitmap(newImageBitmap)
            }
            true
        }

    }
    private fun processImage() {
        val mat = Mat()
        Utils.bitmapToMat(newImageBitmap, mat)
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val faces = MatOfRect()
        faceCascade.detectMultiScale(gray, faces)

        val faceArray = faces.toArray()
        for (face in faceArray) {
            Imgproc.rectangle(mat, face.tl(), face.br(), Scalar(255.0, 0.0, 0.0), 2)
        }

        Utils.matToBitmap(mat, newImageBitmap)
        mainImage.setImageBitmap(newImageBitmap)
    }
}