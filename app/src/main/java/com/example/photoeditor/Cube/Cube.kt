package com.example.photoeditor.Cube

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.MainActivity
import com.example.photoeditor.R
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.Display
import android.view.MotionEvent
import android.view.View
import com.example.photoeditor.Cube.Vertex
import com.example.photoeditor.Cube.DrawView
import kotlin.math.cos
import kotlin.math.sin

class Cube : AppCompatActivity() {
    private lateinit var choosePickButton: Button
    private lateinit var vertexes: Array<Vertex>
    private lateinit var canvas: DrawView
    private val paint = Paint()

    private fun initVertexes() {
        vertexes = arrayOf(
            Vertex(-200f, -200f, -200f),
            Vertex(600f, -200f, -200f),
            Vertex(600f, 600f, -200f),
            Vertex(-200f, 600f, -200f),
            Vertex(-200f, -200f, 600f),
            Vertex(600f, -200f, 600f),
            Vertex(600f, 600f, 600f),
            Vertex(-200f, 600f, 600f)
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
    private val paint = Paint()
    private var previousX = 0f
    private var previousY = 0f
    private var currentX = 0f
    private var currentY = 0f
    val VERTEX_RADIUS = 10f
    private val connections = arrayOf(
        arrayOf(0,4),
        arrayOf(1,5),
        arrayOf(2,6),
        arrayOf(3,7),
        arrayOf(0,1),
        arrayOf(1,2),
        arrayOf(2,3),
        arrayOf(3,0),
        arrayOf(4,5),
        arrayOf(5,6),
        arrayOf(6,7),
        arrayOf(7,4),

    )
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y

                val deltaX = currentX - previousX
                val deltaY = currentY - previousY
                rotateCube(deltaX, deltaY)

                previousX = currentX
                previousY = currentY
            }
        }
        return true
    }
    private fun rotateCube(deltaX: Float, deltaY: Float) {
        val sensitivity = 0.1f

        val angleY = deltaX * sensitivity

        val angleX = deltaY * sensitivity
        vertexes.forEach { rotate(it, angleX, angleY, 0f) }

        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val depthFactor = 0.5f
        val rotatedVertices = vertexes.map { rotate(it, 0f, 0f, 1f) }
        for (vertex in rotatedVertices) {
            val scaledX = centerX + vertex.x * depthFactor
            val scaledY = centerY - vertex.y * depthFactor
            val scaledRadius = VERTEX_RADIUS * (1 - vertex.z / 1000)
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawCircle(scaledX, scaledY, scaledRadius, paint)
        }
        for (connection in connections) {
            val vertex1 = rotatedVertices[connection[0]]
            val vertex2 = rotatedVertices[connection[1]]
            val scaledX1 = centerX + vertex1.x * depthFactor
            val scaledY1 = centerY - vertex1.y * depthFactor
            val scaledX2 = centerX + vertex2.x * depthFactor
            val scaledY2 = centerY - vertex2.y * depthFactor
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            canvas.drawLine(scaledX1, scaledY1, scaledX2, scaledY2, paint)
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