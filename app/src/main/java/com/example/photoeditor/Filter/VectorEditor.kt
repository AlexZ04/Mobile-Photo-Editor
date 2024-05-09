package com.example.photoeditor.Filter

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.Display
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.MainActivity
import com.example.photoeditor.R
import kotlin.math.abs
import kotlin.properties.Delegates


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

        canvas.screenHeight = display.height
        canvas.screenWidth = display.width

        setContentView(canvas)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.x <= display.width / 5 && event.y <= display.height / 10) {
                returnToStart()
            }
            else if (event.y >= display.height * 0.9 && event.action == 0) {
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
    private lateinit var rect : Rect
    private var listOfPoints = mutableListOf <Pair<Float, Float>>()
    private var splinePoints = mutableListOf <Pair<Float, Float>>()

    private var editSplinePoints = mutableListOf <Pair<Float, Float>>()

    var screenHeight by Delegates.notNull<Int>()
    var screenWidth by Delegates.notNull<Int>()

    var mode = 0 // 0 - обычный мод, 1 - расставление точек на сплайнах, 2 - удаление точек

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.BLACK)
        paint.setColor(Color.WHITE)
        paint.strokeWidth = 10F

        rect = Rect(0, 0, screenWidth / 5, screenHeight / 10)

        canvas.drawRect(rect, paint)
        canvas.drawRect(Rect(0, (screenHeight * 0.9).toInt(), screenWidth, screenHeight), paint)

        paint.setColor(Color.GRAY)
        for (i in 1 until listOfPoints.size) {
            canvas.drawLine(listOfPoints[i].first, listOfPoints[i].second,
                listOfPoints[i - 1].first, listOfPoints[i - 1].second, paint)
        }
        
        paint.setColor(Color.GREEN)
        for (i in 1..<splinePoints.size) {
            canvas.drawLine(splinePoints[i].first, splinePoints[i].second,
                splinePoints[i - 1].first, splinePoints[i - 1].second, paint)
        }

        paint.setColor(Color.WHITE)
        for (i in 0..<listOfPoints.size) {
            if (mode == 2) {
                paint.setColor(Color.BLUE)
                canvas.drawCircle(listOfPoints[i].first, listOfPoints[i].second, 25F, paint)
                paint.setColor(Color.WHITE)
            }
            canvas.drawCircle(listOfPoints[i].first, listOfPoints[i].second, 16F, paint)
        }

        paint.setColor(Color.RED)
        for (i in 0..<editSplinePoints.size) {
            if (mode == 2) {
                paint.setColor(Color.BLUE)
                canvas.drawCircle(editSplinePoints[i].first, editSplinePoints[i].second, 25F, paint)
                paint.setColor(Color.WHITE)
            }

            canvas.drawCircle(editSplinePoints[i].first, editSplinePoints[i].second, 16F, paint)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            if ((event.x > screenWidth / 5 || event.y > screenHeight / 10) &&
                event.y < screenHeight * 0.9) {

                if (mode == 0) {
                    listOfPoints = (listOfPoints + (event.x to event.y))
                            as MutableList<Pair<Float, Float>>
                }

                else if (mode == 1) {

                    addSplineEditPoint(event.x to event.y)
//                    editSplinePoints.add(event.x to event.y)
                }

                else if (mode == 2) {
                    deletePoint(event.x to event.y)
                }
                invalidate()
            }

        }
        return super.onTouchEvent(event)
    }

    fun goAlg() { // Алгоритм «де Кастельжо»
        var stepValue = 0F

        mode = 1

        if (listOfPoints.size < 2) {
            splinePoints = mutableListOf <Pair<Float, Float>>()
            return
        }
        if (listOfPoints.size == 2) {
            splinePoints = mutableListOf <Pair<Float, Float>>()
            splinePoints.addAll(listOfPoints)
            invalidate()
            return
        }

        splinePoints = mutableListOf <Pair<Float, Float>>()
        splinePoints = (splinePoints + (listOfPoints[0].first to listOfPoints[0].second))
                as MutableList<Pair<Float, Float>>

        val tempListOfPoints = mutableListOf <Pair<Float, Float>>()
//        tempListOfPoints.addAll(listOfPoints)

        var amountOfPoints = 2

        for (i in 1 until listOfPoints.size) {
            tempListOfPoints.add(listOfPoints[i - 1].first to listOfPoints[i - 1].second)

            amountOfPoints = 2

            while (amountOfPoints > 0) {
                val newX = listOfPoints[i - 1].first + (listOfPoints[i].first -
                        listOfPoints[i - 1].first) * (1 / amountOfPoints)
                val newY = listOfPoints[i - 1].second + (listOfPoints[i].second -
                        listOfPoints[i - 1].second) * (1 / amountOfPoints)

                amountOfPoints--

                tempListOfPoints.add(newX to newY)
            }

        }

        tempListOfPoints.add(listOfPoints[listOfPoints.size - 1].first to
                listOfPoints[listOfPoints.size - 1].second)

        while (stepValue <= 1) {

            var newLinesPoints = mutableListOf <Pair<Float, Float>>()
//            newLinesPoints.addAll(listOfPoints)
            newLinesPoints.addAll(tempListOfPoints)

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

            stepValue += 0.01F
        }

        invalidate()
    }

    fun deletePoint(point: Pair<Float, Float>) {
        for (i in 0 until listOfPoints.size) {
            if (abs(listOfPoints[i].first - point.first) < 25 &&
                abs(listOfPoints[i].second - point.second) < 25) {

                listOfPoints.removeAt(i)
                goAlg()
                invalidate()
                return
            }
        }

        for (i in 0 until editSplinePoints.size) {
            if (abs(editSplinePoints[i].first - point.first) < 16 &&
                abs(editSplinePoints[i].second - point.second) < 16) {

                editSplinePoints.removeAt(i)
                goAlg()
                invalidate()
                return
            }
        }


    }

    fun addSplineEditPoint(point: Pair<Float, Float>) {
        var amountOfPoints = 3

        val tempSplinePoints = mutableListOf <Pair<Float, Float>>()

        for (i in 1 until splinePoints.size) {
            tempSplinePoints.add(splinePoints[i - 1].first to splinePoints[i - 1].second)

            amountOfPoints = 3

            while (amountOfPoints > 0) {
                val newX = splinePoints[i - 1].first + (splinePoints[i].first -
                        splinePoints[i - 1].first) * (1 / amountOfPoints)
                val newY = splinePoints[i - 1].second + (splinePoints[i].second -
                        splinePoints[i - 1].second) * (1 / amountOfPoints)

                amountOfPoints--

                tempSplinePoints.add(newX to newY)
            }

        }

        for (i in 0 until tempSplinePoints.size) {

            if (abs(tempSplinePoints[i].first - point.first) < 16 &&
                abs(tempSplinePoints[i].second - point.second) < 16) {

                editSplinePoints.add(tempSplinePoints[i].first to tempSplinePoints[i].second)
                invalidate()
                return
            }
        }
    }
}
