package com.sprboot.mockkexample.kotlinmockkexample.calc

import org.springframework.stereotype.Component

@Component
class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }

    fun sum(args: List<Int>): Int {
        return args.sum()
    }

    fun multiply(a: Int, b: Int): Int {
        return a * b
    }
}