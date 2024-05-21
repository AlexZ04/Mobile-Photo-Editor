package com.example.photoeditor.Cube

class Vertex constructor(var x: Float = 0.0f, var y: Float = 0.0f, var z: Float = 0.0f) {
    init {
        this.x = x
        this.y = y
        this.z = z
    }
    fun dotProduct(a: Vertex, b: Vertex): Float {
        return a.x * b.x + a.y * b.y + a.z * b.z
    }

    fun normalize(): Vertex {
        val length = Math.sqrt((x * x + y * y + z * z).toDouble())
        return Vertex((x / length).toFloat(), (y / length).toFloat(), (z / length).toFloat())
    }
}