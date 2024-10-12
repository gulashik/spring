package com.sprboot.mockkexample.kotlinmockkexample.controller

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import com.sprboot.mockkexample.kotlinmockkexample.calc.Calculator
import com.sprboot.mockkexample.kotlinmockkexample.domain.User
import com.sprboot.mockkexample.kotlinmockkexample.service.UserService
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean // отдельная зависимость NINJA-SQUAD
    private lateinit var userService: UserService

    @MockkBean(relaxed = true)
    private lateinit var userServiceRelaxed: UserService

    @SpykBean
    private lateinit var calculator: Calculator;

    @BeforeEach
    fun setup() {
        clearMocks(userService) // todo Удалить все следы взаимодействия с моками (например, вызовы методов, проверки состояний и т.д.) между тестами.
    }

    @Test
    fun `test for relaxed option`() {
        // todo использование бина с relaxed = true
        // todo НЕ ИСПОЛЬЗУЕМ every { }
        val user: User = userServiceRelaxed.getUserById(99)
        println(user.id == 0L) // true
        println( user.name == "") // true
    }

    @Test
    fun `test getUserById returns user`() {
        val mockUser = User(1L, "User1")
        val mockUser2 = User(2L, "User2")

        // Настроим MockK чтобы возвратить mockUser при вызове метода
//         every { mock.someMethod(1) } returns "One"
//         every { mock.someMethod() } throws IllegalStateException()
//         every { mock.someMethod(any()) } returnsMany listOf("First", "Second")
//         every { mock.someMethod() } answers { "Answer" }
        every { userService.getUserById(1L) } returns /*todo первый вызов*/mockUser andThen/*todo второй вызов*/ mockUser2

        mockMvc.perform(
                get("/users/1")
            )
            .andDo(print()) // todo отладочная печать
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("User1"))

        mockMvc.perform(
                get("/users/1")
            )
            .andDo(print()) // todo отладочная печать
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("User2"))

        /*todo VERIFY*/
        // todo был вызыван метод
        verify { userService.getUserById(1L) }

        // todo сколько раз
        verify(atLeast = 1) { userService.getUserById(1L) }
        verify(atLeast = 1, atMost = 2) { userService.getUserById(1L) }
        verify(exactly = 2) { userService.getUserById(1L) }

        // todo timeout
        verify(timeout = 20/*milliseconds*/) { userService.getUserById(1L) }

        // todo проверка при вызове аргументов
        verify {
            userService.getUserById(
                withArg { arg: Long ->
                    assertThat(arg).isEqualTo(1L)
                }
            )
        }

        // todo проверка последовательности вызовов
        verifySequence {
            // вызов первого get()
            calculator.add(any(),any())
            userService.getUserById(any())
            // вызов второго get()
            calculator.add(any(),any())
            userService.getUserById(any())
        }
    }
}