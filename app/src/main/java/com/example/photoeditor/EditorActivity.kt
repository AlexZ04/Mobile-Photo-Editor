package com.example.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.Filter.ColorFilters

class EditorActivity : AppCompatActivity() {

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button

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

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, intent.data)
        mainImage.setImageBitmap(bitmap)

        choosePickButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val objectImage = ColorFilters(bitmap)
        objectImage.negativeFilter()
        mainImage.setImageBitmap(objectImage.bitmap)
    }
}