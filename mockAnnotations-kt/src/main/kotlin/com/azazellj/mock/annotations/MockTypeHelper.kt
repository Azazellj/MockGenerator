package com.azazellj.mock.annotations

import kotlin.reflect.KClass


@Target( AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MockTypeHelper(val types: Array<KClass<*>> = [], val initNeeded: Boolean = false)
