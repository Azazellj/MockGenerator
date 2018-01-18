package com.azazellj.test.mockcompiler;

import com.azazellj.mock.annotations.Mock;
import com.azazellj.mock.annotations.MockTypeHelper;
import com.azazellj.mock.annotations.internal.MockUtils;
import com.azazellj.test.mockcompiler.generator.Helper;
import com.azazellj.test.mockcompiler.generator.MockGenerator;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@AutoService(Processor.class)
public class MockCompilerProcessor extends AbstractProcessor {

    Filer mFiler;
    Messager mMessager;
    Elements mElementUtils;
    Types mTypes;

    private Map<String, MockAnnotatedClass> mockClasses = new LinkedHashMap<>();
    public static Map<String, String> registeredFunctions = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        mFiler = env.getFiler();
        mMessager = env.getMessager();
        mElementUtils = env.getElementUtils();
        mTypes = env.getTypeUtils();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        registerDefaultHelpers();
        registerCustomHelpers(env);

        findAnnotatedClasses(env);
        registerHelpersFromMockAnnotatedClasses();

        generateMockClasses();

        return false;
    }

    private void generateMockClasses() {
        try {
            for (MockAnnotatedClass mockClass : mockClasses.values()) {
                MockGenerator.generateClass(mElementUtils,mTypes,mFiler, mMessager, mockClass);
            }
        } catch (IOException ioEx) {
            error(null, ioEx.toString());
        }

        mockClasses.clear();
    }

    private void findAnnotatedClasses(RoundEnvironment env) {
        Set<? extends Element> elements = env.getElementsAnnotatedWith(Mock.class);

        for (Element annotatedElement : elements) {
            if (!isValidAnnotatedElement(annotatedElement)) continue;

            registerMockAnnotatedClass(annotatedElement);
        }
    }

    private void registerHelpersFromMockAnnotatedClasses() {
        for (String preGeneratedClassName : mockClasses.keySet()) {
            String fullClassName = mockClasses.get(preGeneratedClassName).getAnnotatedClassElement().toString();
            String postGeneratedFunction = Helper.fullGeneratedHelperPath(preGeneratedClassName);

            registeredFunctions.put(fullClassName, postGeneratedFunction);
        }
    }

    private void registerMockAnnotatedClass(Element element) {
        MockAnnotatedClass annotatedClass = new MockAnnotatedClass(element);
        String elementName = elementName(element);

        if (mockClasses.containsKey(elementName)) {
            // TODO: 1/7/18 write overriding and prioritizing
        }

        mockClasses.put(elementName, annotatedClass);
    }

    private boolean isValidAnnotatedElement(Element element) {
        boolean isValid = true;
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "Only classes can be annotated with @%s", Mock.class.getSimpleName());
            isValid = false;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            error(element, "Abstract class can`t be annotated with @%s", Mock.class.getSimpleName());
            isValid = false;
        }

        return isValid;
    }

    private void registerDefaultHelpers() {
        TypeElement typeElement = mElementUtils.getTypeElement(MockUtils.class.getName());
        registerFunctions(typeElement.getEnclosedElements());
    }

    private void registerCustomHelpers(RoundEnvironment env) {
        registerFunctions(env.getElementsAnnotatedWith(MockTypeHelper.class));
    }

    void registerFunctions(Collection<? extends Element> elements) {
        for (Element element : elements) {
            if (!isMockHelperMethodValid(element)) continue;

            String helperFunctionName = helperFunctionName(element);
            registerFunction(element, helperFunctionName);
        }
    }

    void registerFunction(Element element, String helperFunctionName) {
        try {
            MockTypeHelper helper = element.getAnnotation(MockTypeHelper.class);
            for (Class supportedClass : helper.types()) {
                // TODO: 1/6/18 write overriding and prioritizing
                registeredFunctions.put(supportedClass.toString(), helperFunctionName);
            }
        } catch (MirroredTypesException typesEx) {
            for (TypeMirror typeMirror : typesEx.getTypeMirrors()) {
                // TODO: 1/6/18 write overriding and prioritizing
                registeredFunctions.put(typeMirror.toString(), helperFunctionName);
            }
        }

//        mMessager.printMessage(Diagnostic.Kind.ERROR, helperFunctionName);


    }


    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(Mock.class.getCanonicalName());
        annotations.add(MockTypeHelper.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported();}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String elementName(Element element) {
        return checkNotNull(element).getSimpleName().toString();
    }

    boolean isMockHelperMethodValid(Element element) {
        checkNotNull(element);

        boolean isMethod = element.getKind() == ElementKind.METHOD;
        boolean isStatic = element.getModifiers().contains(Modifier.STATIC);
        boolean isPublic = element.getModifiers().contains(Modifier.PUBLIC);
        return isMethod && isPublic && isStatic;
    }

    String helperFunctionName(Element element) {
        checkNotNull(element);
        checkNotNull(element.getEnclosingElement());

        String className = element.getEnclosingElement().asType().toString();
        String functionName = element.toString();
        return String.format("%s.%s", className, functionName);
    }
}
