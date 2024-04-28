package com.example.photoeditor.Filter

import android.R.attr.bitmap
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.photoeditor.MainActivity
import com.example.photoeditor.R


class VectorEditor : AppCompatActivity() {

    private lateinit var canvas : ImageView
    private lateinit var display: Display
    private lateinit var canvasBitmap: Bitmap
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vector_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        canvas = findViewById(R.id.canvasId)

        display = windowManager.getDefaultDisplay()
        canvasBitmap = Bitmap.createBitmap(display.width, display.height - 150, Bitmap.Config.ARGB_8888)

        for (x in 0 until canvasBitmap.getWidth()) {
            for (y in 0 until canvasBitmap.getHeight()) {
                val color = canvasBitmap.getPixel(x, y)

                canvasBitmap.setPixel(x, y, Color.rgb(255, 255, 125))
            }
        }

        canvas.setImageBitmap(canvasBitmap)
//        setContentView(DrawView(this));

        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        println(event)
        return super.onTouchEvent(event)
    }


    internal class DrawView(context: Context?) : View(context) {

        private val paint = Paint()
        private val rect = Rect()
        private var listOfPoints = mutableListOf(-1F to -1F)

        override fun onDraw(canvas: Canvas) {

            canvas.drawColor(Color.BLACK)
            paint.setColor(Color.WHITE)
            for (i in 1..<listOfPoints.size) {
                canvas.drawCircle(listOfPoints[i].first, listOfPoints[i].second, 10F, paint)
            }

        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            if (event != null) {
                val pair = event.x to event.y

                listOfPoints = (listOfPoints + (event.x to event.y)) as MutableList<Pair<Float, Float>>
                invalidate()
            }
            return super.onTouchEvent(event)
        }

    }


}