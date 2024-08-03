package com.sprboot.mockkexample.kotlinmockkexample.args

import com.sprboot.mockkexample.kotlinmockkexample.calc.Calculator
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SlotTest {

    // todo моки
    private val calculatorSpy: Calculator = spyk<Calculator>()

    private val calculatorMock: Calculator = mockk<Calculator>()

    @BeforeEach
    fun setup() {
        clearAllMocks()  // todo Удалить все следы взаимодействия с моками (например, вызовы методов, проверки состояний и т.д.) между тестами.
    }

    @Test
    fun `test capture + slot option`() {
        // todo слот/место куда кладём перехваченный аргумент
        val slotOneSpy = slot<Int>()
        val slotTwoSpy = slot<Int>()
        val slotOneMock = slot<Int>()
        val slotTwoMock = slot<Int>()

        every {
            calculatorSpy.add(/*todo складываем в слот*/capture(slotOneSpy), /*todo складываем в слот*/capture(slotTwoSpy))
        } returns 0/*ответ не важен*/
        every {
            calculatorMock.add(/*todo складываем в слот*/capture(slotOneMock), /*todo складываем в слот*/capture(slotTwoMock))
        } returns 0/*ответ не важен*/

        calculatorSpy.add(10, 20)
        assertEquals(
            /*todo получаем что было на вход*/
            slotOneSpy.captured.also(::println) // 10
            , 10
        )
        assertEquals(
            slotTwoSpy.captured.also(::println) // 20
            , 20
        )

        calculatorMock.add(100, 200)
        assertEquals(
            slotOneMock.captured.also(::println) // 100
            , 100
        )
        assertEquals(
            slotTwoMock.captured.also(::println) // 200
            , 200
        )
    }

    @Test
    fun `test capture + collection or capture + slot(collection)`() {
        // todo mutable коллекция куда кладём аргументы - несколько вызовов
        val collectionManyCallsOne: MutableList<Int> = mutableListOf<Int>()
        val collectionManyCallsTwo: MutableList<Int> = mutableListOf<Int>()

        // todo слот/место куда кладём перехваченный аргумент-коллекция - один вызов
        val capturingSlot: CapturingSlot<List<Int>> = slot<List<Int>>()

        /*todo Коллекция как Аргумент===============================================*/
        every {
            calculatorMock.sum(/*todo коллекция на вход - один вызов*/capture(capturingSlot))
        } returns 0

        calculatorMock.sum(listOf(1, 2, 3))

        assertEquals(
            /*todo получаем что было на вход*/
            capturingSlot.captured.also(::println) // [1, 2, 3]
            , listOf(1, 2, 3)
        )

        /*todo Коллекция содержит Одиночные вызовы по Аргументу=======================*/
        every {
            calculatorMock.add(
                /*todo кладём РЕЗУЛЬТАТЫ ОДИНОЧНЫХ вызовов*/
                capture(collectionManyCallsOne),
                capture(collectionManyCallsTwo)
            )
        } returns 0

        calculatorMock.add(1, 2)
        calculatorMock.add(11, 22)
        calculatorMock.add(111, 222)

        assertEquals(
            collectionManyCallsOne.also(::println), // [1, 11, 111]
            listOf(1, 11, 111)
        )
        assertEquals(
            collectionManyCallsTwo.also(::println), // [2, 22, 222]
            listOf(2, 22, 222)
        )
    }
}

