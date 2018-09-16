package ru.hse.spb

import java.lang.Math.sqrt
import java.util.*

data class Input(val x: Double, val y: Double, val vMax: Double,
                 val timeOfTheFirstWind: Double, val uX: Double,
                 val uY: Double, val wX: Double, val wY: Double)

fun readInput(): Input {
    val scanner = Scanner(System.`in`)
    val startPointX = scanner.nextInt()
    val startPointY = scanner.nextInt()
    val endPointX = scanner.nextInt()
    val endPointY = scanner.nextInt()
    val vMax = scanner.nextInt()
    val time = scanner.nextInt()
    val ux = scanner.nextInt()
    val uy = scanner.nextInt()
    val wx = scanner.nextInt()
    val wy = scanner.nextInt()
    return Input(
            (endPointX - startPointX).toDouble(),
            (endPointY - startPointY).toDouble(),
            vMax.toDouble(),
            time.toDouble(),
            ux.toDouble(),
            uy.toDouble(),
            wx.toDouble(),
            wy.toDouble()
    )
}

class Solver(private val input: Input) {
    companion object {
        const val LEFT_INITIAL = 0.0
        const val RIGHT_INITIAL = 1e9;
        const val NUM_OF_ITERATIONS = 100;
    }

    private fun isReachable(x: Double, y: Double,
                            time: Double): Boolean {
        return sqrt(x * x + y * y) / input.vMax <= time
    }

    private fun isValidTime(time: Double): Boolean {
        if (time < input.timeOfTheFirstWind) {
            val x = input.x - time * input.uX
            val y = input.y - time * input.uY
            return isReachable(x, y, time)
        }
        val timeOfTheSecondWind = time - input.timeOfTheFirstWind;
        val x = input.x - input.timeOfTheFirstWind * input.uX -
                timeOfTheSecondWind * input.wX
        val y = input.y - input.timeOfTheFirstWind * input.uY -
                timeOfTheSecondWind * input.wY
        return isReachable(x, y, time)
    }

    private fun binarySearch(): Double {
        var left = LEFT_INITIAL;
        var right = RIGHT_INITIAL;
        for (i in 1..NUM_OF_ITERATIONS) {
            val median = (left + right) / 2;
            if (isValidTime(median)) {
                right = median;
            } else {
                left = median;
            }
        }
        return right;
    }

    fun solve() {
        println(binarySearch())
    }
}

fun main(args: Array<String>) {
    val solver = Solver(readInput())
    solver.solve()
}