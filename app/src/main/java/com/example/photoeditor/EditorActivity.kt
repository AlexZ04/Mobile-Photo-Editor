package com.example.photoeditor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.Affine.AffineTransformations
import com.example.photoeditor.Translate.Resize
import com.example.photoeditor.Translate.Rotate


class EditorActivity : AppCompatActivity() {

    private lateinit var rotatingButton: Button
    private lateinit var rotationConfirmButton: Button
    private lateinit var rotationAngleValueText: EditText

    private lateinit var resizingButton: Button
    private lateinit var resizingConfirmButton: Button
    private lateinit var resizingAngleValueText: EditText

    private lateinit var faceDetectorButton: Button

    private lateinit var filtersButton: Button

    private lateinit var affineButton: Button
    private lateinit var firstAffineImage: ImageView
    private lateinit var secondAffineImage: ImageView
    private lateinit var firstAffineChangeButton: Button
    private lateinit var secondAffineChangeButton: Button
    private lateinit var confirmAffineButton: Button

    private lateinit var retouchButton: Button

    private lateinit var unsharpMasking: Button

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button

    private var currAlg: Int = 0

    private fun changeVisibility(elems: Array<View>, isActive: Boolean){

        if(isActive){
            elems.forEach {elem ->
                elem.visibility = View.VISIBLE
            }
        }
        else{
            elems.forEach {elem ->
                elem.visibility = View.INVISIBLE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rotatingButton = findViewById(R.id.rotationButton)
        rotationConfirmButton = findViewById(R.id.rotationConfirmButton)
        rotationAngleValueText = findViewById(R.id.rotationAngleInput)

        resizingButton = findViewById(R.id.resizingButton)
        resizingConfirmButton = findViewById(R.id.resizingConfirmButton)
        resizingAngleValueText = findViewById(R.id.resizingAngleInput)

        faceDetectorButton = findViewById(R.id.faceDetectorButton)
        filtersButton = findViewById(R.id.filtersButton)

        affineButton = findViewById(R.id.affineButton)
        firstAffineImage = findViewById(R.id.affineFirst)
        secondAffineImage = findViewById(R.id.affineSecond)
        firstAffineChangeButton = findViewById(R.id.firstAffineChangeButton)
        secondAffineChangeButton = findViewById(R.id.secondAffineChangeButton)
        confirmAffineButton = findViewById(R.id.confirmAffineButton)

        retouchButton = findViewById(R.id.retouchButton)
        unsharpMasking = findViewById(R.id.unsharpMasking)

        mainImage = findViewById(R.id.mainImage)
        choosePickButton = findViewById(R.id.choosePickButton)

        val views = arrayOf<Array<View>>(

            arrayOf<View>(
                mainImage,
                rotationConfirmButton,
                rotationAngleValueText
            ),

            arrayOf<View>(),

            arrayOf<View>(
                mainImage,
                resizingConfirmButton,
                resizingAngleValueText),

            arrayOf<View>(),
            arrayOf<View>(),
            arrayOf<View>(),

            arrayOf<View>(
                firstAffineImage,
                secondAffineImage,
                firstAffineChangeButton,
                secondAffineChangeButton,
                confirmAffineButton
            )
        )

        val changeAlgorithmButtons = arrayOf<Button>(
            rotatingButton, filtersButton, resizingButton,
            faceDetectorButton, retouchButton, unsharpMasking, affineButton
        )

        for(i in changeAlgorithmButtons.indices){
            changeAlgorithmButtons[i].setOnClickListener(){

                changeVisibility(views[currAlg], false)
                currAlg = i
                changeVisibility(views[currAlg], true)
            }
        }

        val uri: Uri = intent.data!!

        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

        val exif = ExifInterface(contentResolver.openInputStream(uri)!!)
        val orientation: Int =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = Rotate.rotate(bitmap,90.0)
            ExifInterface.ORIENTATION_ROTATE_180 -> bitmap = Rotate.rotate(bitmap,180.0)
            ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = Rotate.rotate(bitmap,270.0)
        }

        mainImage.setImageBitmap(bitmap)
        firstAffineImage.setImageBitmap(bitmap)
        secondAffineImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rotationConfirmButton.setOnClickListener{

            val width = bitmap.width
            val height = bitmap.height
            bitmap = Rotate.rotate(bitmap, -rotationAngleValueText.text.toString().toDouble())
            bitmap = Rotate.rerotate(bitmap, -rotationAngleValueText.text.toString().toDouble(), width, height)
            mainImage.setImageBitmap(bitmap)
        }

        resizingConfirmButton.setOnClickListener{

            mainImage.setImageBitmap(Resize.resize(bitmap, resizingAngleValueText.text.toString().toDouble()))
        }

        var affineMod = 0
        var firstPoints = mutableListOf<Array<Float>>()
        var secondPoints = mutableListOf<Array<Float>>()

        firstAffineChangeButton.setOnClickListener{
            affineMod = 1
            firstPoints.clear()
        }
        secondAffineChangeButton.setOnClickListener{
            affineMod = 2
            secondPoints.clear()
        }

        firstAffineImage.setOnTouchListener { v, event ->

            if (affineMod == 1 && event.action == MotionEvent.ACTION_DOWN) {

                val drawable = firstAffineImage.drawable
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight

                val imageWidth = firstAffineImage.width
                val imageHeight = firstAffineImage.height

                val x = (event.x / imageWidth * intrinsicWidth)
                val y = (event.y / imageHeight * intrinsicHeight)

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                val canvas = Canvas(mutableBitmap)

                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(x, y, 15f, paint)

                firstAffineImage.setImageBitmap(mutableBitmap)
                firstAffineImage.invalidate()

                firstPoints.add(arrayOf(x, y))
                if(firstPoints.size == 3){
                    affineMod = 0
                }

                true

            } else {
                false
            }
        }

        secondAffineImage.setOnTouchListener { v, event ->

            if (affineMod == 2 && event.action == MotionEvent.ACTION_DOWN) {

                val drawable = secondAffineImage.drawable
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight

                val imageWidth = secondAffineImage.width
                val imageHeight = secondAffineImage.height

                val x = (event.x / imageWidth * intrinsicWidth)
                val y = (event.y / imageHeight * intrinsicHeight)

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                val canvas = Canvas(mutableBitmap)

                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(x, y, 15f, paint)

                secondAffineImage.setImageBitmap(mutableBitmap)
                secondAffineImage.invalidate()

                secondPoints.add(arrayOf(x, y))
                if(secondPoints.size == 3){
                    affineMod = 0
                }

                true

            } else {
                false
            }
        }

        confirmAffineButton.setOnClickListener{

            secondAffineImage.setImageBitmap(AffineTransformations.transform(bitmap, firstPoints, secondPoints))
        }
    }
}