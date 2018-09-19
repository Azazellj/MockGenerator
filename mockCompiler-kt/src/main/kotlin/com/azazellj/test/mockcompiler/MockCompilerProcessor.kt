package com.azazellj.test.mockcompiler

import com.azazellj.mock.annotations.Mock
import com.azazellj.mock.annotations.MockTypeHelper
import com.azazellj.mock.annotations.internal.MockUtils
import com.azazellj.test.mockcompiler.generator.Helper
import com.azazellj.test.mockcompiler.generator.MockGenerator
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@AutoService(Processor::class)
class MockCompilerProcessor : AbstractProcessor() {

    companion object {
        val registeredFunctions: MutableMap<String, String> = mutableMapOf()
    }

    lateinit var mFiler: Filer
    lateinit var mMessager: Messager
    lateinit var mElementUtils: Elements
    lateinit var mTypes: Types

    private val mockClasses: MutableMap<String, MockAnnotatedClass> = mutableMapOf()


    override fun init(env: ProcessingEnvironment) {
        super.init(env)

        mFiler = env.filer
        mMessager = env.messager
        mElementUtils = env.elementUtils
        mTypes = env.typeUtils
    }


    override fun process(annotations: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        registerDefaultHelpers()
        registerCustomHelpers(env)

        findAnnotatedClasses(env)
        registerHelpersFromMockAnnotatedClasses()

        generateMockClasses()

        return false
    }

    private fun generateMockClasses() {
        try {
            mockClasses.values.forEach { MockGenerator.generateClass(mElementUtils, mTypes, mFiler, mMessager, it) }
        } catch (anyEx: Exception) {
            e(null, anyEx.toString())
        }

        mockClasses.clear()
    }

    private fun findAnnotatedClasses(env: RoundEnvironment) {
        env.getElementsAnnotatedWith(Mock::class.java)
                .filter { it.isValidAnnotatedElement() }
                .forEach { registerMockAnnotatedClass(it) }
    }

    private fun registerHelpersFromMockAnnotatedClasses() {
        mockClasses.keys.forEach {
            val fullClassName = mockClasses[it]!!.annotatedClassElement.toString()
            val postGeneratedFunction = Helper.fullGeneratedHelperPath(it)
            registeredFunctions[fullClassName] = postGeneratedFunction
        }
    }

    private fun registerMockAnnotatedClass(element: Element) {
        val annotatedClass = MockAnnotatedClass(element)

        if (mockClasses.containsKey(element.elementName())) {
        }

        mockClasses[element.elementName()] = annotatedClass
    }

    private fun registerDefaultHelpers() {
        val typeElement: TypeElement = mElementUtils.getTypeElement(MockUtils::javaClass.name)
        registerFunctions(typeElement.enclosedElements)
    }

    private fun registerCustomHelpers(env: RoundEnvironment) {
        registerFunctions(env.getElementsAnnotatedWith(MockTypeHelper::class.java))
    }

    fun registerFunctions(elements: Collection<out Element>) {
        elements
                .filter { it.isMockHelperMethodValid() }
                .forEach { registerFunction(it, it.helperFunctionName()) }
    }

    fun registerFunction(element: Element, helperFunctionName: String) {
        try {
            val helper: MockTypeHelper = element.getAnnotation(MockTypeHelper::class.java)
            helper.types.forEach { registeredFunctions.put(it.toString(), helperFunctionName) }
        } catch (typesEx: MirroredTypesException) {
            typesEx.typeMirrors.forEach { registeredFunctions.put(it.toString(), helperFunctionName) }
        }
    }

    private fun e(e: Element?, msg: String, vararg args: Any) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, *args),
                e)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf<String>().apply {
            add(Mock::javaClass.name)
            add(MockTypeHelper::javaClass.name)
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun Element?.elementName(): String {
        return checkNotNull(this).simpleName.toString()
    }

    fun Element?.isMockHelperMethodValid(): Boolean {
        checkNotNull(this)

        val isMethod = this!!.kind == ElementKind.METHOD
        val isPublic = this.modifiers.contains(Modifier.PUBLIC)
        val isStatic = this.modifiers.contains(Modifier.STATIC)

        return isMethod && isPublic && isStatic
    }

    fun Element?.helperFunctionName(): String {
        checkNotNull(this)
        checkNotNull(this!!.enclosingElement)

        val className = this.enclosingElement!!.asType().toString()
        val functionName = this.toString()

        return "$className.$functionName"
    }

    fun Element.isValidAnnotatedElement(): Boolean {
        var isValid = true

        if (this.kind != ElementKind.CLASS) {
            e(this, "Only classes can be annotated with @${Mock::javaClass.name}")
            isValid = false
        }
        if (this.modifiers.contains(Modifier.PRIVATE)) {
            e(this, "Private class can`t be annotated with @${Mock::javaClass.name}")
            isValid = false
        }

        return isValid
    }
}