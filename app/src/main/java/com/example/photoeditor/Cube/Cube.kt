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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlinx.coroutines.processNextEventInCurrentThread
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates

class Cube : AppCompatActivity() {
    private lateinit var choosePickButton: Button
    private lateinit var vertexes: Array<Vertex>

    private lateinit var canvas: DrawView
    private val paint = Paint()

    private lateinit var display: Display

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

        display = windowManager.getDefaultDisplay()

        choosePickButton = findViewById(R.id.choosePickButton)
        choosePickButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        initVertexes()
        canvas = DrawView(this, vertexes)

        canvas.screenHeight = display.height
        canvas.screenWidth = display.width

        canvas.fromCube = this

        setContentView(canvas)
    }

    fun returnToStart() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
class DrawView(context: Context, private val vertexes: Array<Vertex>) : View(context) {
    private var scaleDetector: ScaleGestureDetector
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
    private val pointPath = android.graphics.Path()

    var screenHeight by Delegates.notNull<Int>()
    var screenWidth by Delegates.notNull<Int>()

    var fromCube by Delegates.notNull<Cube>()
    init {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        })
    }
    private var previousX = 0f
    private var previousY = 0f
    private var currentX = 0f
    private var currentY = 0f
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
    private var previousX1 = 0f
    private var previousY1 = 0f
    private var previousX2 = 0f
    private var previousY2 = 0f
    private var isScaling = false
    private val handler = Handler(Looper.getMainLooper())
    private val delayAfterScaling = 700L
    private val maxRotationAngle = 130f
    val centerY = height / 2f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (event.x <= screenWidth / 5 && event.y <= screenHeight / 10) {
            fromCube.returnToStart()
        }

        if (event.pointerCount == 2) {
            var rot = 1f;
            val finger1X = event.getX(0)
            val finger1Y = event.getY(0)
            val finger2X = event.getX(1)
            val finger2Y = event.getY(1)
            if (event.getX(1) < event.getX(0)){
                rot = -1f;
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    isScaling = true
                    previousX1 = finger1X
                    previousY1 = finger1Y
                    previousX2 = finger2X
                    previousY2 = finger2Y
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    handler.postDelayed({
                        isScaling = false
                    }, delayAfterScaling)
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentX1 = finger1X
                    val currentY1 = finger1Y
                    val currentX2 = finger2X
                    val currentY2 = finger2Y
                    val bothFingersMoveInSameDirection =
                        (previousX1 < centerY && previousX2 < centerY && currentX1 < centerY && currentX2 < centerY) ||
                                (previousX1 > centerY && previousX2 > centerY && currentX1 > centerY && currentX2 > centerY)
                    var angle = calculateRotationAngle(previousX1, previousY1, currentX1, currentY1,
                        previousX2, previousY2, currentX2, currentY2)
                    if (!bothFingersMoveInSameDirection) {
                        angle *= -1f
                    }
                    rotateZ(angle * rot)

                    previousX1 = currentX1
                    previousY1 = currentY1
                    previousX2 = currentX2
                    previousY2 = currentY2
                }
            }
        } else if (!isScaling) {
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
                    val maxDelta = 40f
                    val clampedDeltaX = deltaX.coerceIn(-maxDelta, maxDelta)
                    val clampedDeltaY = deltaY.coerceIn(-maxDelta, maxDelta)
                    if (!scaleDetector.isInProgress) {
                        rotateCube(clampedDeltaX, clampedDeltaY)
                        previousX = currentX
                        previousY = currentY
                    }
                }
            }
        }
        return true
    }
    private fun calculateRotationAngle(
        prevX1: Float, prevY1: Float, currX1: Float, currY1: Float,
        prevX2: Float, prevY2: Float, currX2: Float, currY2: Float
    ): Float {
        val angle1 = Math.atan2((currY1 - prevY1).toDouble(), (currX1 - prevX1).toDouble())
        val angle2 = Math.atan2((currY2 - prevY2).toDouble(), (currX2 - prevX2).toDouble())
        var angle = Math.toDegrees(angle1 - angle2).toFloat()
        angle = angle.coerceIn(-maxRotationAngle, maxRotationAngle)
        val distance1 = Math.hypot((currX1 - prevX1).toDouble(), (currY1 - prevY1).toDouble()).toFloat()
        val distance2 = Math.hypot((currX2 - prevX2).toDouble(), (currY2 - prevY2).toDouble()).toFloat()

        val averageDistance = (distance1 + distance2) / 2
        val rotationSpeed = averageDistance * 0.001f

        return angle * rotationSpeed
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

        canvas.drawColor(Color.BLACK)
        val cameraDirection = Vertex(0f, 0f, 100f)
        val centerX = width / 2f
        val centerY = height / 2f
        faceVisibility.fill(false)
        for (i in connections.indices) {
            val connection = connections[i]
            val vertex1 = vertexes[connection[0]]
            val vertex2 = vertexes[connection[1]]
            val vertex3 = vertexes[connection[2]]

            val v1 = Vertex(vertex1.x, vertex1.y, vertex1.z)
            val v2 = Vertex(vertex2.x, vertex2.y, vertex2.z)
            val v3 = Vertex(vertex3.x, vertex3.y, vertex3.z)

            val t1 = Vertex(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z)
            val t2 = Vertex(v2.x - v3.x, v2.y - v3.y, v2.z - v3.z)

            val normal = crossProduct(t2, t1).normalize()

            val dotProduct = dotProduct(cameraDirection, normal)

            if (dotProduct > 0) {
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = triangleColors[i]
                paint.alpha = 255
                val x1 = centerX + vertex1.x
                val y1 = centerY - vertex1.y
                val x2 = centerX + vertex2.x
                val y2 = centerY - vertex2.y
                val x3 = centerX + vertex3.x
                val y3 = centerY - vertex3.y

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

        paint.setColor(Color.WHITE)
        paint.strokeWidth = 10F
        canvas.drawLine((screenWidth * 2 / 25).toFloat(), (screenHeight / 20).toFloat(),
            (screenWidth * 4 / 25).toFloat(), (screenHeight / 20 / 2).toFloat(), paint)
        canvas.drawLine((screenWidth * 2 / 25).toFloat(), (screenHeight / 20).toFloat(),
            (screenWidth * 4 / 25).toFloat(), (screenHeight / 20 + screenHeight / 20 / 2).toFloat(), paint)
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
        val sin = sin(toRadians(angle.toDouble()))
        val cos = cos(toRadians(angle.toDouble()))

        for (vertex in vertexes) {
            val x = vertex.x
            val y = vertex.y

            vertex.x = (x * cos - y * sin).toFloat()
            vertex.y = (y * cos + x * sin).toFloat()
        }
        for (digit in digits) {
            for (point in digit) {
                val x = point.x
                val y = point.y

                point.x = (x * cos - y * sin).toFloat()
                point.y = (y * cos + x * sin).toFloat()
            }
        }
        invalidate()
    }
    private fun rotate(vertex: Vertex, angleX: Float, angleY: Float, angleZ: Float): Vertex {
        val radX = toRadians(angleX.toDouble()).toFloat()
        val radY = toRadians(angleY.toDouble()).toFloat()
        val radZ = toRadians(angleZ.toDouble()).toFloat()

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