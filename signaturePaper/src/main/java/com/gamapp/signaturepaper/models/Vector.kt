package com.gamapp.signaturepaper.models

import kotlin.math.sin
import kotlin.math.sqrt


/**
 * A vector,
 * @param dx is Delta in X axis
 * @param dy id Delta in Y axis
 * */
data class Vector constructor(val dx: Float, val dy: Float) {
    constructor(v: Vector) : this(v.dx, v.dy)

    val size by lazy {
        sqrt(dx * dx + dy * dy)
    }

    fun perSize(): Vector {
        return Vector(dx / size, dy / size)
    }

    fun rotateBy(angle: Float): Vector {
        return Vector(
            dx * kotlin.math.cos(angle) - dy * sin(angle),
            dx * sin(angle) + dy * kotlin.math.cos(angle)
        )
    }

    fun getVertical(vector: Vector): Vector {
        return if (dy * vector.dy + -dx * vector.dx > 0) {
            Vector(dy, -dx)
        } else {
            Vector(-dy, dx)
        }
    }

    fun cos(v: Vector): Float {
        return (dx * v.dx + dy * v.dy) / (v.size * this.size)
    }

    operator fun minus(vector: Vector): Vector {
        return Vector(dx - vector.dx, dy - vector.dy)
    }

    operator fun plus(vector: Vector): Vector {
        return Vector(dx + vector.dx, dy + vector.dy)
    }

    operator fun times(number: Float): Vector {
        return Vector(number * dx, number * dy)
    }

    operator fun div(number: Float): Vector {
        return Vector(number / dx, number / dy)
    }
}