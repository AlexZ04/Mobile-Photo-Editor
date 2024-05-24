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
import kotlin.math.pow
import kotlin.math.sqrt
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
        var currentPoint = 0F to 0F
        var checkPoint = 0F to 0F

        if (event != null) {

            currentPoint = event.x to event.y

            if (event.x <= display.width / 5 && event.y <= display.height / 10) {
                returnToStart()
            }
            else if (event.y >= display.height * 0.9 && display.width / 4 > event.x && event.action == 0) {
                canvas.goAlg()
            }
            else if (event.y >= display.height * 0.9 && display.width * 2 / 4 > event.x && event.action == 0) {
                canvas.mode = 0
                canvas.invalidate()
            }
            else if (event.y >= display.height * 0.9 && display.width * 3 / 4 > event.x && event.action == 0) {
                canvas.mode = 1
                canvas.invalidate()
            }
            else if (event.y >= display.height * 0.9 && event.action == 0) {
                canvas.mode = 2
                canvas.invalidate()
            }
            else if (canvas.mode == 1) {
                if (event.action == 0) {
                    if (canvas.hasPoint(currentPoint)) {
                        canvas.movePoint(currentPoint)
                        canvas.foundPoint = true
                    }
                    else {
                        canvas.foundPoint = false
                    }
                }
                else if (event.action == 2 && canvas.foundPoint) {
                    canvas.movePoint(currentPoint)
                    canvas.addNewPointFromEdit(currentPoint)
                }
                else if (canvas.foundPoint){
                    canvas.movePoint(0F to 0F)
                    canvas.foundPoint = false
                    canvas.isCurrentPoint = false
                    canvas.addNewPointFromEdit(0F to 0F)
                }

            }
        }
        return super.onTouchEvent(event)
    }

    private fun returnToStart() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

internal class DrawView(context: Context?) : View(context) {

    private val paint = Paint()
    private val fontPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var rect : Rect
    private var listOfPoints = mutableListOf <Pair<Float, Float>>()
    private var splinePoints = mutableListOf <Pair<Float, Float>>()

    private var listOfRealPoints = mutableListOf <Pair<Float, Float>>()

    var foundPoint = false

    private var editSplinePoint = 0F to 0F

    var screenHeight by Delegates.notNull<Int>()
    var screenWidth by Delegates.notNull<Int>()

    var isCurrentPoint = false

    private var newAddedEditPoint = 0F to 0F

    var mode = 0 // 0 - добавление точек в кривую, 1 - расставление точек на сплайнах, 2 - удаление точек

    override fun onDraw(canvas: Canvas) {

        fontPaint.textSize = (screenWidth / 27).toFloat()
        fontPaint.setColor(Color.BLACK)
        fontPaint.textAlign = Paint.Align.CENTER

        canvas.drawColor(Color.BLACK)
        paint.setColor(Color.WHITE)
        paint.strokeWidth = 10F

        paint.setColor(Color.WHITE)

        canvas.drawLine((screenWidth * 2 / 25).toFloat(), (screenHeight / 20).toFloat(),
            (screenWidth * 4 / 25).toFloat(), (screenHeight / 20 / 2).toFloat(), paint)
        canvas.drawLine((screenWidth * 2 / 25).toFloat(), (screenHeight / 20).toFloat(),
            (screenWidth * 4 / 25).toFloat(), (screenHeight / 20 + screenHeight / 20 / 2).toFloat(), paint)


        canvas.drawRect(Rect(0, (screenHeight * 0.9).toInt(), screenWidth, screenHeight), paint)

        canvas.drawText("Build spline", (screenWidth * 0.5 / 4).toFloat(), (screenHeight * 9.5 / 10).toFloat(), fontPaint)
        canvas.drawText("Add points", (screenWidth * 1.5 / 4).toFloat(), (screenHeight * 9.5 / 10).toFloat(), fontPaint)
        canvas.drawText("Edit spline", (screenWidth * 2.5 / 4).toFloat(), (screenHeight * 9.5 / 10).toFloat(), fontPaint)
        canvas.drawText("Delete points", (screenWidth * 3.5 / 4).toFloat(), (screenHeight * 9.5 / 10).toFloat(), fontPaint)

        paint.setColor(Color.BLACK)
        canvas.drawLine((screenWidth / 4).toFloat(), (screenHeight * 0.9).toFloat(),
            (screenWidth / 4).toFloat(), screenHeight.toFloat(), paint)

        canvas.drawLine((screenWidth * 2 / 4).toFloat(), (screenHeight * 0.9).toFloat(),
            (screenWidth * 2 / 4).toFloat(), screenHeight.toFloat(), paint)

        canvas.drawLine((screenWidth * 3 / 4).toFloat(), (screenHeight * 0.9).toFloat(),
            (screenWidth * 3 / 4).toFloat(), screenHeight.toFloat(), paint)


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
        if (editSplinePoint != 0F to 0F) {
            canvas.drawCircle(editSplinePoint.first, editSplinePoint.second, 25F, paint)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            if ((event.x > screenWidth / 5 || event.y > screenHeight / 10) &&
                event.y < screenHeight * 0.9) {

                if (mode == 0) {
                    listOfPoints.add(event.x to event.y)
                    listOfRealPoints.add(event.x to event.y)
                }

                else if (mode == 1) {

//                    addSplineEditPoint(event.x to event.y)
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

        if (listOfPoints.size < 2) {
            splinePoints = mutableListOf <Pair<Float, Float>>()
            return
        }

        splinePoints = mutableListOf <Pair<Float, Float>>()
        splinePoints.add(listOfRealPoints[0].first to listOfRealPoints[0].second)

        if (newAddedEditPoint != 0F to 0F) {
            putPointIn(newAddedEditPoint)
        }

        val tempListOfPoints = mutableListOf <Pair<Float, Float>>()

        var amountOfPoints = 2

        for (i in 1 until listOfRealPoints.size) {
            tempListOfPoints.add(listOfRealPoints[i - 1].first to listOfRealPoints[i - 1].second)

            amountOfPoints = 2

            while (amountOfPoints > 0) {
                val newX = listOfRealPoints[i - 1].first + (listOfRealPoints[i].first -
                        listOfRealPoints[i - 1].first) * (1 / amountOfPoints)
                val newY = listOfRealPoints[i - 1].second + (listOfRealPoints[i].second -
                        listOfRealPoints[i - 1].second) * (1 / amountOfPoints)

                amountOfPoints--

                tempListOfPoints.add(newX to newY)
            }

        }

        tempListOfPoints.add(listOfRealPoints[listOfRealPoints.size - 1].first to
                listOfRealPoints[listOfRealPoints.size - 1].second)

        while (stepValue <= 1) {

            var newLinesPoints = mutableListOf <Pair<Float, Float>>()

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

            splinePoints.add(newPointX to newPointY)

            stepValue += 0.01F
        }

        invalidate()

        if (isCurrentPoint) {
            listOfRealPoints.remove(newAddedEditPoint)
        }
    }

    private fun deletePoint(point: Pair<Float, Float>) {
        for (i in 0 until listOfPoints.size) {
            if (abs(listOfPoints[i].first - point.first) < 25 &&
                abs(listOfPoints[i].second - point.second) < 25) {

                listOfPoints.removeAt(i)
                listOfRealPoints = mutableListOf <Pair<Float, Float>>()
                listOfRealPoints.addAll(listOfPoints)
                goAlg()
                invalidate()
                return
            }
        }

    }

    fun movePoint(newCoord: Pair<Float, Float>) {
        editSplinePoint = newCoord
        invalidate()
    }

    fun hasPoint(point: Pair<Float, Float>): Boolean {
        if (splinePoints.size == 0) {
            return false
        }

        var amountOfPoints = 3

        val tempSplinePoints = mutableListOf <Pair<Float, Float>>()

        for (i in 1 until splinePoints.size) {
            tempSplinePoints.add(splinePoints[i - 1])

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

        tempSplinePoints.add(splinePoints[splinePoints.size - 1])

        for (i in 0 until tempSplinePoints.size) {
            if (abs(point.first - tempSplinePoints[i].first) <= 25 &&
                abs(point.second - tempSplinePoints[i].second) <= 25) {
                return true
            }
        }

        return false
    }

    fun addNewPointFromEdit(point: Pair<Float, Float>) {
        if (point == 0F to 0F) {
            goAlg()
            newAddedEditPoint = point
            return
        }
        if (!isCurrentPoint) { // если рассматриваем текущую точку,
            // то изменяем координаты новой точки, иначе - добавляем
            isCurrentPoint = true
        }

        newAddedEditPoint = point
        goAlg()
    }

    private fun putPointIn(point: Pair<Float, Float>) {
        val tempRealPoints = mutableListOf <Pair<Float, Float>>()
        val index = findPosition(point)

        for (i in 0 until listOfRealPoints.size) {
            tempRealPoints.add(listOfRealPoints[i])

            if (i == index) {
                tempRealPoints.add(point)
            }
        }

        listOfRealPoints = mutableListOf <Pair<Float, Float>>()
        listOfRealPoints.addAll(tempRealPoints)

    }

    private fun findPosition(point: Pair<Float, Float>): Int {

        var index = 0
        var minDist = 100000F
        var sumDistToPoints = 100000F

        var currentDist = 0F
        var currentSumDist = 0F

        for (i in 1 until listOfRealPoints.size) {

            currentDist = getDistFromPointToSegment(point, listOfRealPoints[i], listOfRealPoints[i - 1])

            currentSumDist = getDist(point, listOfRealPoints[i]) + getDist(point,
                listOfRealPoints[i - 1])

            if (currentDist < minDist || (currentDist == minDist && currentSumDist < sumDistToPoints)) {
                minDist = currentDist
                sumDistToPoints = currentSumDist
                index = i - 1
            }
        }

        return index
    }

    private fun getDist(firstPoint: Pair<Float, Float>, secondPoint: Pair<Float, Float>): Float {
        return sqrt((firstPoint.first - secondPoint.first).pow(2) +
                (firstPoint.second - secondPoint.second).pow(2))
    }

    private fun getDistFromPointToSegment(
        point: Pair<Float, Float>, firstPoint: Pair<Float, Float>, secondPoint: Pair<Float, Float>)
    : Float {

        if (checkTriangle(point, firstPoint, secondPoint)) { // если можно опустить высоту -
            // - ищем высоту
            if (firstPoint.first - secondPoint.first == 0F) {
                return getDist(point, firstPoint.second to point.second)
            }
            else if (firstPoint.second - secondPoint.second == 0F) {
                return getDist(point, point.first to firstPoint.second)
            }
            else {
                val deltaX = firstPoint.first - secondPoint.first
                val deltaY = firstPoint.second - secondPoint.second

                val newY = (deltaX * (firstPoint.second * (deltaX / deltaY) - firstPoint.first + point.first) +
                        deltaY * point.second) / ((deltaX * deltaX / deltaY) + deltaY)

                val newX = (newY - firstPoint.second) * (deltaX / deltaY) + firstPoint.first

                return getDist(point, newX to newY)
            }
        }
        // иначе - возвращаем расстояние до одного из концов отрезка
        return Math.min(getDist(point, firstPoint), getDist(point, secondPoint))
    }

    // проверяем, можно ли опустить высоту на отрезок
    private fun checkTriangle(point: Pair<Float, Float>, firstPoint: Pair<Float, Float>,
                              secondPoint: Pair<Float, Float>): Boolean {
        val dist1 = getDist(point, firstPoint)
        val dist2 = getDist(point, secondPoint)

        val dist3 = getDist(firstPoint, secondPoint)

        return !(dist3.pow(2) + dist1.pow(2) - dist2.pow(2) < 0 ||
                dist3.pow(2) + dist2.pow(2) - dist1.pow(2) < 0)
    }

}
