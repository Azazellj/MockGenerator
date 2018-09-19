package com.azazellj.test.mockcompiler.generator


object Helper {
    const val OBJECT_NAME: String = "someObject"
    val GENERATED_PACKAGE: String = Helper.javaClass.`package`.name

    fun generatedMockClassName(className: String): String {
        return "Mock$className"
    }

    fun generatedMockGetter(): String {
        return MockObject::class.java.methods[0].name
    }

    fun fullGeneratedHelperPath(className: String): String {
        return "$GENERATED_PACKAGE.${generatedMockClassName(className)}.${generatedMockGetter()}()"
    }
}

