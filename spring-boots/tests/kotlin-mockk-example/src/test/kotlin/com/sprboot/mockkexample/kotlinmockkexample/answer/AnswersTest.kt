package com.sprboot.mockkexample.kotlinmockkexample.answer

import com.sprboot.mockkexample.kotlinmockkexample.domain.User
import com.sprboot.mockkexample.kotlinmockkexample.service.UserService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class AnswersTest {

    // todo мок реального объекта
    private val userService = mockk<UserService>()

    @BeforeEach
    fun setup() {
        clearMocks(userService) // todo Удалить все следы взаимодействия с моками (например, вызовы методов, проверки состояний и т.д.) между тестами.
    }

    @Test
    fun `test answer returns`() {
        // todo Использование answers
        every { userService.getUserById(any()) } answers {

            // todo Входные аргументы
            //  inline fun <reified T> arg(n: Int) = invocation.args[n] as T
            //  inline fun <reified T> firstArg() = invocation.args[0] as T
            //  inline fun <reified T> secondArg() = invocation.args[1] as T
            //  inline fun <reified T> thirdArg() = invocation.args[2] as T
            //  inline fun <reified T> lastArg() = invocation.args.last() as T
            //  val arg = call.invocation.args[0] as Int  // Доступ к аргументу через Call
            val firstArgument = firstArg<Long>()
            val nthArg: Long = arg<Long>(0)

            // Получили первым аргументом
            println("Get first arg = $firstArgument")
            println("Get first arg = $nthArg")

            // todo Последним то что возвращаем
            User(1, "User1")
        }

        with( userService.getUserById(90).also(::println) ) {
            assertThat(id).isEqualTo(1L)
            assertThat(name).isEqualTo("User1")
        }
    }
}