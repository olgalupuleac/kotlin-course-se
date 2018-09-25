package ru.hse.spb

import java.lang.Math.sqrt
import java.util.*

/**
 * Class which represents an input for a Solver.
 * We suppose that start point is (0, 0).
 * x - x coordinate of the endpoint
 * y - y coordinate of the endpoint.
 * vMax - the max speed of the plane (actual speed,
 * because there is no reason to move slower)
 * timeOfTheFirstWind - time, which the first blows
 * uX - x component of the first wind's speed
 * uY - y component of the first wind's speed
 * wX - x component of the second wind's speed
 * wY - y component of the second wind's speed
 */
data class Input(val x: Double, val y: Double, val vMax: Double,
                 val timeOfTheFirstWind: Double, val uX: Double,
                 val uY: Double, val wX: Double, val wY: Double)

/**
 * Reads an input from the command line,
 * sets a start point to (0, 0) and returns an instance of Input
 * to be passed to Solver.
 */
private fun readInput(): Input {
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

/**
 * Class which takes an input and solves the problem.
 * http://codeforces.com/contest/590/problem/B
 */
class Solver(private val input: Input) {
    /**
     * Constants used in binary search.
     * Calculated according to required precision and
     * input and time limits.
     */
    companion object {
        const val LEFT_INITIAL = 0.0
        const val RIGHT_INITIAL = 1e9
        const val NUM_OF_ITERATIONS = 100
    }

    /**
     * Checks if the specified point is reachable in the specified
     * time if the plane goes with the max speed.
     */
    private fun isReachable(x: Double, y: Double,
                            time: Double): Boolean {
        return sqrt(x * x + y * y) / input.vMax <= time
    }

    /**
     * Checks if it is possible to reach the endpoint in the
     * specified time.
     *
     * If the time is less than input.timeOfTheFirstWind,
     * we should consider only displacement caused by the first wind.
     * Otherwise, we should consider displacements caused by both winds.
     */
    private fun isValidTime(time: Double): Boolean {
        if (time < input.timeOfTheFirstWind) {
            val x = input.x - time * input.uX
            val y = input.y - time * input.uY
            return isReachable(x, y, time)
        }
        val timeOfTheSecondWind = time - input.timeOfTheFirstWind
        val x = input.x - input.timeOfTheFirstWind * input.uX -
                timeOfTheSecondWind * input.wX
        val y = input.y - input.timeOfTheFirstWind * input.uY -
                timeOfTheSecondWind * input.wY
        return isReachable(x, y, time)
    }

    /**
     * Makes a binary search
     * of the time in which the endpoint can be reached.
     */
    fun binarySearch(): Double {
        var left = LEFT_INITIAL
        var right = RIGHT_INITIAL
        for (i in 1..NUM_OF_ITERATIONS) {
            val median = (left + right) / 2
            if (isValidTime(median)) {
                right = median
            } else {
                left = median
            }
        }
        return right
    }
}

fun main(args: Array<String>) {
    val solver = Solver(readInput())
    println(solver.binarySearch())
}