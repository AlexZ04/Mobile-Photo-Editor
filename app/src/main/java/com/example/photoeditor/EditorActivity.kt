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

class EditorActivity : AppCompatActivity() {

    private lateinit var mainImage: ImageView
    private lateinit var choosePickButton: Button
    private lateinit var colorFilterButton: Button

    private lateinit var newImageBitmap: Bitmap

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
        colorFilterButton = findViewById(R.id.colorFilterButton)

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
        mainImage.setOnTouchListener { v, event ->
            // Обработка события нажатия
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                val retouching = Retouching(newImageBitmap)
                newImageBitmap = retouching.startRetouching(x, y, 30, 20)

                    // Установка измененного изображения в ImageView
                mainImage.setImageBitmap(newImageBitmap)
            }
            true
        }

    }
}