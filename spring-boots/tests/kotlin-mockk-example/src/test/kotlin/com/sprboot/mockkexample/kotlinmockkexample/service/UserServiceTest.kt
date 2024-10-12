package com.sprboot.mockkexample.kotlinmockkexample.service

import com.sprboot.mockkexample.kotlinmockkexample.domain.User
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var mockUserService: UserService

    @BeforeEach
    fun setup() {
        mockUserService = mockk()
        userService = UserService()
    }

    @Test
    fun `test getUserById returns correct user`() {
        val mockUser = User(1L, "User1")

        every { mockUserService.getUserById(1L) } returns mockUser

        val result = mockUserService.getUserById(1L)
        assertEquals("User1", result.name)
        assertEquals(1L, result.id)

        verify { mockUserService.getUserById(1L) }
    }
}