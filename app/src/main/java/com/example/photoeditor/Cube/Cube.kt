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
            Vertex(-200f, 200f, -200f),
            Vertex(200f, 200f, -200f),
            Vertex(200f, -200f, -200f),
            Vertex(-200f, -200f, 200f),
            Vertex(-200f, 200f, 200f),
            Vertex(200f, 200f, 200f),
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
    private val paint = Paint()
    val VERTEX_RADIUS = 10f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val depthFactor = 0.5f
        val rotatedVertices = vertexes.map { rotate(it, 2f, 1f, 4f) }
        for (vertex in vertexes) {
            val scaledX = centerX + vertex.x * depthFactor
            val scaledY = centerY - vertex.y * depthFactor
            val scaledRadius = VERTEX_RADIUS * (1 - vertex.z / 1000)
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawCircle(scaledX, scaledY, scaledRadius, paint)
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