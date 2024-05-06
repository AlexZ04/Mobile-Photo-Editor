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
import kotlin.math.abs


class VectorEditor : AppCompatActivity() {

//    private lateinit var canvas : ImageView
    private lateinit var display: Display

    private lateinit var canvas : DrawView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vector_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        display = windowManager.getDefaultDisplay()
        canvas = DrawView(this)

        setContentView(canvas)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.x <= 200 && event.y <= 200) {
                returnToStart()
            }
            else if (event.y >= 2000 && event.action == 0) {
                canvas.goAlg()
            }
        }
        return super.onTouchEvent(event)
    }

    fun returnToStart() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

internal class DrawView(context: Context?) : View(context) {

    private val paint = Paint()
    private val rect = Rect(0, 0, 200, 200)
//    private var listOfPoints = mutableListOf(-1F to -1F)
    private var listOfPoints = mutableListOf <Pair<Float, Float>>()
    private var splinePoints = mutableListOf <Pair<Float, Float>>()

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.BLACK)
        paint.setColor(Color.WHITE)
        paint.strokeWidth = 10F

        canvas.drawRect(rect, paint)
        canvas.drawRect(Rect(0, 2000, 5000, 5000), paint)


        paint.setColor(Color.WHITE)
        for (i in 0..<listOfPoints.size) {
            canvas.drawCircle(listOfPoints[i].first, listOfPoints[i].second, 16F, paint)
        }

        paint.setColor(Color.GRAY)
        for (i in 1 until listOfPoints.size) {
            canvas.drawLine(listOfPoints[i].first, listOfPoints[i].second,
                listOfPoints[i - 1].first, listOfPoints[i - 1].second, paint)
        }

        paint.setColor(Color.GREEN)
        for (i in 1..<splinePoints.size) {
//            canvas.drawCircle(splinePoints[i].first, splinePoints[i].second, 16F, paint)
            canvas.drawLine(splinePoints[i].first, splinePoints[i].second,
                splinePoints[i - 1].first, splinePoints[i - 1].second, paint)
        }

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            if ((event.x > 200 || event.y > 200) && event.y < 2000) {
                listOfPoints = (listOfPoints + (event.x to event.y)) as MutableList<Pair<Float, Float>>
                invalidate()
            }

        }
        return super.onTouchEvent(event)
    }

    fun goAlg() { // Алгоритм «де Кастельжо»
        var stepValue = 0F

        if (listOfPoints.size < 2) return
        if (listOfPoints.size == 2) {
            splinePoints = mutableListOf <Pair<Float, Float>>()
            splinePoints.addAll(listOfPoints)
            return
        }

        splinePoints = mutableListOf <Pair<Float, Float>>()
        splinePoints = (splinePoints + (listOfPoints[0].first to listOfPoints[0].second))
                as MutableList<Pair<Float, Float>>

        while (stepValue < 1) {

            var newLinesPoints = mutableListOf <Pair<Float, Float>>()
            newLinesPoints.addAll(listOfPoints)

            var newTempList = mutableListOf <Pair<Float, Float>>()

            while (newLinesPoints.size != 3) {

                for (i in 1 until newLinesPoints.size) {
                    val newX = newLinesPoints[i - 1].first + (newLinesPoints[i].first -
                            newLinesPoints[i - 1].first) * stepValue
                    val newY = newLinesPoints[i - 1].second + (newLinesPoints[i].second -
                            newLinesPoints[i - 1].second) * stepValue

                    newTempList = (newTempList + (newX to newY))
                            as MutableList<Pair<Float, Float>>
                }

                newLinesPoints = mutableListOf <Pair<Float, Float>>()
                newLinesPoints.addAll(newTempList)

                newTempList.clear()
            }

            val firstPointX = newLinesPoints[0].first +
                    (newLinesPoints[1].first - newLinesPoints[0].first) * stepValue
            val firstPointY = newLinesPoints[0].second +
                    (newLinesPoints[1].second - newLinesPoints[0].second) * stepValue

            val secondPointX = newLinesPoints[1].first +
                    (newLinesPoints[2].first - newLinesPoints[1].first) * stepValue
            val secondPointY = newLinesPoints[1].second +
                    (newLinesPoints[2].second - newLinesPoints[1].second) * stepValue

            val newPointX = firstPointX + (secondPointX - firstPointX) * stepValue
            val newPointY = firstPointY + (secondPointY - firstPointY) * stepValue

            splinePoints = (splinePoints + (newPointX to newPointY)) as MutableList<Pair<Float, Float>>

            stepValue += 0.05F
        }

        invalidate()
    }

}
