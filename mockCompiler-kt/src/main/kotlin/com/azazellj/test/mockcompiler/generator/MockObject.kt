package com.azazellj.test.mockcompiler.generator

interface MockObject<out T> {
    val getMock: T
}