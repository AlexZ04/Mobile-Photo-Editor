package com.example.photoeditor

import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button
    private lateinit var confirmButton: Button
    private lateinit var angleValueText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainImage = findViewById(R.id.mainImage)
        choosePickButton = findViewById(R.id.choosePickButton)
        confirmButton = findViewById(R.id.confirmButton)
        angleValueText = findViewById(R.id.angleInput)

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

//        mainImage.setImageBitmap(bitmap)
//
//        choosePickButton.setOnClickListener{
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
//
//        confirmButton.setOnClickListener{
//
//            mainImage.setImageBitmap(objectImage.rotate(-angleValueText.text.toString().toDouble()))
//        }

        mainImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        confirmButton.setOnClickListener{

            mainImage.setImageBitmap(Resize.resize(bitmap, angleValueText.text.toString().toDouble()))
        }
    }
}