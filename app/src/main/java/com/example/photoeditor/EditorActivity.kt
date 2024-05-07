package com.example.photoeditor

import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        retouchButton = findViewById(R.id.retouchButton)
        unsharpMasking = findViewById(R.id.unsharpMasking)

        mainImage = findViewById(R.id.mainImage)
        choosePickButton = findViewById(R.id.choosePickButton)

        val views = arrayOf<Array<View>>(

            arrayOf<View>(
                rotationConfirmButton,
                rotationAngleValueText
            ),

            arrayOf<View>(
                resizingConfirmButton,
                resizingAngleValueText
            ),

            arrayOf<View>(),
            arrayOf<View>(),
            arrayOf<View>(),
            arrayOf<View>(),
            arrayOf<View>()
        )

        rotatingButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 0
            changeVisibility(views[currAlg], true)
        }
        filtersButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 1
            changeVisibility(views[currAlg], true)
        }
        resizingButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 2
            changeVisibility(views[currAlg], true)
        }
        faceDetectorButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 3
            changeVisibility(views[currAlg], true)
        }
        retouchButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 4
            changeVisibility(views[currAlg], true)
        }
        unsharpMasking.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 5
            changeVisibility(views[currAlg], true)
        }
        affineButton.setOnClickListener(){
            changeVisibility(views[currAlg], false)
            currAlg = 6
            changeVisibility(views[currAlg], true)
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

//        confirmButton.setOnClickListener{
//
//            mainImage.setImageBitmap(objectImage.rotate(-angleValueText.text.toString().toDouble()))
//        }

        mainImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rotationConfirmButton.setOnClickListener{

            mainImage.setImageBitmap(Rotate.rotate(bitmap, -rotationAngleValueText.text.toString().toDouble()))
        }

        resizingConfirmButton.setOnClickListener{

            mainImage.setImageBitmap(Resize.resize(bitmap, resizingAngleValueText.text.toString().toDouble()))
        }
    }
}