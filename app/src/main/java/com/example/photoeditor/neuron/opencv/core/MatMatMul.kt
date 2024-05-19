package com.example.photoeditor.neuron.opencv.core

operator fun Mat.times(other: Mat): Mat = this.matMul(other)
