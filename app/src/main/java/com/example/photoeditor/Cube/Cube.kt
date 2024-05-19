package com.example.photoeditor.Cube

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.photoeditor.MainActivity
import com.example.photoeditor.R
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.lang.Math.toRadians
import kotlin.io.path.Path
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Cube : AppCompatActivity() {
    private lateinit var choosePickButton: Button
    private lateinit var vertexes: Array<Vertex>

    private lateinit var canvas: DrawView
    private val paint = Paint()

    private fun initVertexes() {
        vertexes = arrayOf(
            Vertex(-200f, 200f, 200f),
            Vertex(-200f, 200f, -200f),
            Vertex(200f, 200f, -200f),
            Vertex(200f, 200f, 200f),
            Vertex(-200f, -200f, 200f),
            Vertex(-200f, -200f, -200f),
            Vertex(200f, -200f, -200f),
            Vertex(200f, -200f, 200f)
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cube)

        choosePickButton = findViewById(R.id.choosePickButton)
        choosePickButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        initVertexes()
        canvas = DrawView(this, vertexes)
        setContentView(canvas)
    }
    private fun drawVertex(canvas: Canvas) {
        val VERTEX_RADIUS = 15f
        for (vertex in vertexes) {
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawCircle(vertex.x, vertex.y, VERTEX_RADIUS, paint)
        }
    }


}
class DrawView(context: Context, private val vertexes: Array<Vertex>) : View(context) {
    private var scaleDetector: ScaleGestureDetector
    private var cameraDistance = 100000f
    private val triangleColors = arrayOf(
        Color.RED, Color.RED,
        Color.GREEN, Color.GREEN,
        Color.BLUE, Color.BLUE,
        Color.YELLOW, Color.YELLOW,
        Color.MAGENTA, Color.MAGENTA,
        Color.CYAN, Color.CYAN
    )
    private var digits = arrayOf(
        arrayOf(Vertex(80f, 200f, -120f),Vertex(-80f, 200f, -120f),Vertex(-80f, 200f, 0f),Vertex(80f, 200f, 0f),Vertex(80f, 200f, 120f),Vertex(-80f, 200f, 120f)),
        arrayOf(Vertex(-80f, -200f, 120f),Vertex(-80f, -200f, 0f),Vertex(80f, -200f, 0f),Vertex(80f, -200f, 120f),Vertex(80f, -200f, -120f)),
        arrayOf(Vertex(-200f, 120f, -80f),Vertex(-200f, 120f, 80f),Vertex(-200f, 0f, 80f),Vertex(-200f, -120f, -80f),Vertex(-200f, -120f, 80f)),
        arrayOf(Vertex(80f, 120f, -200f),Vertex(80f, -120f, -200f),Vertex(-80f, -120f, -200f),Vertex(-80f, 120f, -200f), Vertex(80f, 120f, -200f)),
        arrayOf(Vertex(200f, 120f, 80f),Vertex(200f, 120f, -80f),Vertex(200f, 0f, 80f),Vertex(200f, 0f, -80f),Vertex(200f, -120f, 80f)),
        arrayOf(Vertex(-70f, 50f, 200f),Vertex(40f, 150f, 200f),Vertex(40f, -150f, 200f))
    )
    private var faceVisibility = BooleanArray(6) { false }
    private val paint = Paint()
    private var scale = 1f
    private val pointPath = android.graphics.Path()
    init {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                cameraDistance /= detector.scaleFactor  // Обновляем расстояние камеры вместо масштаба
                cameraDistance = Math.max(2000f, Math.min(cameraDistance, 2000f))  // Ограничиваем расстояние камеры
                invalidate()
                return true
            }
        })
    }
    private var previousX = 0f
    private var previousY = 0f
    private var currentX = 0f
    private var currentY = 0f
    val VERTEX_RADIUS = 10f
    private val connections = arrayOf(
        intArrayOf(0, 1, 2),
        intArrayOf(0, 2, 3),
        intArrayOf(4, 6, 5),
        intArrayOf(4, 7, 6),
        intArrayOf(0, 5, 1),
        intArrayOf(0, 4, 5),
        intArrayOf(1, 5, 2),
        intArrayOf(6, 2, 5),
        intArrayOf(3, 2, 6),
        intArrayOf(3, 6, 7),
        intArrayOf(3, 4, 0),
        intArrayOf(4, 3, 7)
    )
    private val faceConnections = arrayOf(
        intArrayOf(0, 1),
        intArrayOf(2, 3),
        intArrayOf(4, 5),
        intArrayOf(6, 7),
        intArrayOf(8, 9),
        intArrayOf(10, 11)
    )
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (event.pointerCount == 2) {
            val finger1X = event.getX(0)
            val finger1Y = event.getY(0)
            val finger2X = event.getX(1)
            val finger2Y = event.getY(1)

            var initialUpperFingerX = 0f
            var initialUpperFingerY = 0f
            var upperFingerMovingRight = false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (finger1Y < finger2Y) {
                        initialUpperFingerX = finger1X
                        initialUpperFingerY = finger1Y
                    } else {
                        initialUpperFingerX = finger2X
                        initialUpperFingerY = finger2Y
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    var deltaX = 0
                    var deltaY = 0
                    var currentUpperFingerX = event.getX(event.findPointerIndex(0))
                    var currentUpperFingerY = event.getY(event.findPointerIndex(0))
                    if (finger1Y < finger2Y) {
                        currentUpperFingerX = event.getX(event.findPointerIndex(1))
                        currentUpperFingerY = event.getY(event.findPointerIndex(1))
                        deltaX = (finger1X - currentUpperFingerX).toInt()
                        deltaY = (finger1Y - currentUpperFingerY).toInt()
                    } else {
                        currentUpperFingerX = event.getX(event.findPointerIndex(0))
                        currentUpperFingerY = event.getY(event.findPointerIndex(0))
                        deltaX = (finger2X - currentUpperFingerX).toInt()
                        deltaY = (finger2Y - currentUpperFingerY).toInt()
                    }
                    upperFingerMovingRight = deltaX < 0
                }
            }

            val angle = 1f
            if (upperFingerMovingRight) {
                rotateZ(angle)
            } else {
                rotateZ(-angle)
            }
        } else if (!scaleDetector.isInProgress) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    currentX = event.x
                    currentY = event.y

                    val deltaX = currentX - previousX
                    val deltaY = currentY - previousY
                    if (!scaleDetector.isInProgress) {
                        rotateCube(deltaX, deltaY)
                        previousX = currentX
                        previousY = currentY
                    }
                }
            }
        }
        return true
    }
    private fun rotateCube(deltaX: Float, deltaY: Float) {
        val sensitivity = 0.1f

        val angleY = deltaX * sensitivity

        val angleX = deltaY * sensitivity
        vertexes.forEach { rotate(it, angleX, angleY, 0f) }
        digits.forEach { it.forEach { rotate(it, angleX, angleY, 0f) } }

        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        val cameraDirection = Vertex(0f, 0f, 1f)
        val centerX = width / 2f
        val centerY = height / 2f
        val canvasCenterX = width / 2f
        val canvasCenterY = height / 2f
        faceVisibility.fill(false)
        // Рисование линий между вершинами с учетом масштаба
        for (i in connections.indices) {
            val connection = connections[i]
            val vertex1 = vertexes[connection[0]]
            val vertex2 = vertexes[connection[1]]
            val vertex3 = vertexes[connection[2]]

            val v1 = Vertex(vertex1.x, vertex1.y, vertex1.z)
            val v2 = Vertex(vertex2.x, vertex2.y, vertex2.z)
            val v3 = Vertex(vertex3.x, vertex3.y, vertex3.z)

            // Вычисляем вектора, образующие грань
            val t1 = Vertex(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z)
            val t2 = Vertex(v2.x - v3.x, v2.y - v3.y, v2.z - v3.z)

            // Вычисляем нормаль к грани
            val normal = crossProduct(t2, t1).normalize()

            // Вычисляем скалярное произведение вектора камеры на нормаль
            val dotProduct = dotProduct(cameraDirection, normal)

            if (dotProduct > 0) {
                // Грань видима, рисуем ее
//                val zFactor1 = cameraDistance / (cameraDistance - vertex1.z)
//                val zFactor2 = cameraDistance / (cameraDistance - vertex2.z)
//                val zFactor3 = cameraDistance / (cameraDistance - vertex3.z)
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = triangleColors[i]
                paint.alpha = 255
                val x1 = centerX + vertex1.x //* zFactor1
                val y1 = centerY - vertex1.y //* zFactor1
                val x2 = centerX + vertex2.x //* zFactor2
                val y2 = centerY - vertex2.y //* zFactor2
                val x3 = centerX + vertex3.x //* zFactor3
                val y3 = centerY - vertex3.y //* zFactor3

                canvas.drawLine(x1, y1, x2, y2, paint)
                canvas.drawLine(x2, y2, x3, y3, paint)
                canvas.drawLine(x3, y3, x1, y1, paint)
                pointPath.reset()
                pointPath.moveTo(x1, y1)
                pointPath.lineTo(x2, y2)
                pointPath.lineTo(x3, y3)
                pointPath.close()
                canvas.drawPath(pointPath, paint)
                val digitPaint = Paint()
                digitPaint.color = Color.BLACK
                digitPaint.strokeWidth = 10f
                // Проверяем, какие цифры нужно отобразить
                for (j in faceConnections.indices) {
                    val faceConnection = faceConnections[j]
                    if (i == faceConnection[0] || i == faceConnection[1]) {
                        for (i in digits[j].indices) {
                            val currentElement = digits[j][i]
                            faceVisibility[j] = true
                            val scaledFromX = centerX + currentElement.x
                            val scaledFromY = centerY - currentElement.y

                            if (i < digits[j].size - 1) {
                                val nextElement = digits[j][i + 1]
                                val scaledToX = centerX + nextElement.x
                                val scaledToY = centerY - nextElement.y

                                canvas.drawLine(scaledFromX, scaledFromY, scaledToX, scaledToY, digitPaint)
                            }
                            canvas.drawCircle(scaledFromX, scaledFromY, 5f, digitPaint)
                        }

                    }

                }
            }
            val digitPaint = Paint()
            digitPaint.color = Color.BLACK
            for (i in faceVisibility.indices) {
                Log.d("FaceVisibility", "Face $i is ${if (faceVisibility[i]) "visible" else "invisible"}")
            }
        }
        //invalidate()
    }
    private fun fillPolygon(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, color: Int) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.alpha = 255

        val minX = minOf(x1, x2, x3).toInt()
        val maxX = maxOf(x1, x2, x3).toInt()
        val minY = minOf(y1, y2, y3).toInt()
        val maxY = maxOf(y1, y2, y3).toInt()

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                if (isPointInTriangle(x1, y1, x2, y2, x3, y3, x.toFloat(), y.toFloat())) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)
                    Log.d("fillPolygon", "Drawing point at ($x, $y)")
                }
                continue;
            }
        }
    }

    private fun isPointInTriangle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float, px: Float, py: Float): Boolean {
        val bX = bx - ax
        val bY = by - ay
        val cX = cx - ax
        val cY = cy - ay
        val pX = px - ax
        val pY = py - ay

        val m = (pX * bY - bX * pY) / (cX * bY - bX * cY)
        if (m >= 0 && m <= 1) {
            val l = (pX - m * cX) / bX
            if (l >= 0 && (m + l) <= 1) {
                return true
            }
        }
        return false
    }
    fun dotProduct(a: Vertex, b: Vertex): Float {
        return a.x * b.x + a.y * b.y + a.z * b.z
    }
    fun crossProduct(v1: Vertex, v2: Vertex): Vertex {
        val x = v1.y * v2.z - v1.z * v2.y
        val y = v1.z * v2.x - v1.x * v2.z
        val z = v1.x * v2.y - v1.y * v2.x
        return Vertex(x, y, z)
    }
    fun rotateZ(angle: Float) {
        //val angle = 1f
        val sin = sin(toRadians(angle.toDouble()))
        val cos = cos(toRadians(angle.toDouble()))

        for (vertex in vertexes) {
            val x = vertex.x
            val y = vertex.y

            vertex.x = (x * cos - y * sin).toFloat()
            vertex.y = (y * cos + x * sin).toFloat()
        }

        invalidate()
    }
    private fun rotate(vertex: Vertex, angleX: Float, angleY: Float, angleZ: Float): Vertex {
        val radX = Math.toRadians(angleX.toDouble()).toFloat()
        val radY = Math.toRadians(angleY.toDouble()).toFloat()
        val radZ = Math.toRadians(angleZ.toDouble()).toFloat()

        val newY = cos(radX) * vertex.y - sin(radX) * vertex.z
        val newZ = sin(radX) * vertex.y + cos(radX) * vertex.z
        vertex.y = newY
        vertex.z = newZ

        val newX = cos(radY) * vertex.x + sin(radY) * vertex.z
        vertex.z = cos(radY) * vertex.z - sin(radY) * vertex.x
        vertex.x = newX

        val finalX = cos(radZ) * vertex.x - sin(radZ) * vertex.y
        val finalY = sin(radZ) * vertex.x + cos(radZ) * vertex.y
        vertex.x = finalX
        vertex.y = finalY

        return vertex
    }
}