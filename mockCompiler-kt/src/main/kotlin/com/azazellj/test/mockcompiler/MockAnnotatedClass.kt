package com.azazellj.test.mockcompiler

import com.azazellj.mock.annotations.Mock
import javax.lang.model.element.Element
import kotlin.reflect.KClass

class MockAnnotatedClass(element: Element) {
    var annotatedClassElement: Element = element
    var ignoredTypes: Array<KClass<*>>

    init {
        val mock: Mock = element.getAnnotation(Mock::class.java)
        ignoredTypes = mock.ignoredTypes
    }

}