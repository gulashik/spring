package com.sprboot.mockkexample.kotlinmockkexample.args

import com.sprboot.mockkexample.kotlinmockkexample.calc.Calculator
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalculatorTest {
    // todo Spy-мок реального объекта
    val calculator = spyk<Calculator>()

    @BeforeEach
    fun setup() {
        clearMocks(calculator) // todo Удалить все следы взаимодействия с моками (например, вызовы методов, проверки состояний и т.д.) между тестами.
    }

    @Test
    fun `test for usung spy`() {
        // Мокаем только метод multiply
        every { calculator.multiply(2, 3) } returns 10

        println(calculator.add(2, 3)) // Выведет 5, реальный метод
        println(calculator.multiply(2, 3)) // Выведет 10, замоканный метод
    }
}