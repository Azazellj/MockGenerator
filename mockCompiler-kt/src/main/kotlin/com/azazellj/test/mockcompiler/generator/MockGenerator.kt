package com.azazellj.test.mockcompiler.generator

import com.azazellj.mock.annotations.GeneratedMock
import com.azazellj.mock.annotations.MockTypeHelper
import com.azazellj.test.mockcompiler.MockAnnotatedClass
import com.azazellj.test.mockcompiler.MockCompilerProcessor
import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

object MockGenerator {


    private fun e(messager: Messager, vararg key: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, key.toString())
    }

    fun generateCLass(elements: Elements, types: Types, filer: Filer, messager: Messager, mockClass:
    MockAnnotatedClass) {
        val typeElement = mockClass.annotatedClassElement
        val className = typeElement.simpleName.toString()
        val classType: TypeName = TypeName.get(typeElement.asType())

        val builder = MethodSpec.methodBuilder(Helper.generatedMockGetter())
                .returns(classType)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addCode(CodeBlock.builder().build())
                .addStatement("\$T \$L = new \$T()", classType, Helper.OBJECT_NAME, classType)

        typeElement.enclosedElements
                .filter { it.kind.isField }
                .forEach {


                    val nullOrFunction = nullOrHelperFunction(it)

                    if (nullOrFunction == null) {
                        val comment = "For field ${it.simpleName} value is set to null."
                        builder.addComment(comment)
                        messager.printMessage(Diagnostic.Kind.WARNING, comment)
                    }

                    builder.addStatement("return \$L", Helper.OBJECT_NAME)


                    val codeBlock = CodeBlock.builder().add("{\$L.class}", className).build()
                    val specs = AnnotationSpec.builder(MockTypeHelper::class.java).addMember("types", codeBlock).build()
                    val methodGetMock = builder.addAnnotation(specs).build()
                    val generatedClass = TypeSpec.classBuilder(Helper.generatedMockClassName(className))
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .addAnnotation(GeneratedMock::class.java)
                            .addMethod(methodGetMock)
                            .build()

                    val generatedFile = JavaFile.builder(Helper.GENERATED_PACKAGE, generatedClass).build()

                    try {
                        generatedFile.writeTo(filer)
                    } catch (ioEx: IOException) {
                        e(messager, ioEx.toString())
                    }
                }
    }

    private fun nullOrHelperFunction(field: Element): String? {
        return MockCompilerProcessor.registeredFunctions[field.asType().toString()]
    }
}