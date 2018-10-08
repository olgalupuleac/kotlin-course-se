package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test

class TestSource {
    @Test
    fun bothWinds() {
        val solver = Solver(Input(5.0, 5.0,
                3.0, 2.0,
                -1.0, -1.0,
                -1.0, 0.0))
        assertEquals(3.729935587093555327, solver.binarySearch(), 1e-6)
    }

    @Test
    fun onlyOneWind() {
        val solver = Solver(Input(0.0, 1000.0,
                100.0, 1000.0,
                -50.0, 0.0,
                50.0, 0.0))
        assertEquals(11.547005383792516398, solver.binarySearch(), 1e-6)
    }

    @Test
    fun zeroAnswer() {
        val solver = Solver(Input(0.0, 0.0,
                10.0, 100.0,
                5.0, 0.0,
                0.0, 0.0))
        assertEquals(0.0, solver.binarySearch(), 1e-6)
    }

    @Test
    fun bigInput() {
        val solver = Solver(Input(
                -20000.0, 20000.0,
                1000.0, 999.0,
                0.0, -999.0,
                999.0, 0.0
        ))
        assertEquals(1018.7770495642339483, solver.binarySearch(), 1e-6)
    }
}