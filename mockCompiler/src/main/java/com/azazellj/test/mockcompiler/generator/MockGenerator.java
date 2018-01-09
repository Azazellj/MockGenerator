package com.azazellj.test.mockcompiler.generator;

import com.azazellj.mock.annotations.GeneratedMock;
import com.azazellj.mock.annotations.MockTypeHelper;
import com.azazellj.test.mockcompiler.MockAnnotatedClass;
import com.azazellj.test.mockcompiler.MockCompilerProcessor;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Method;

public class MockGenerator {

    public static void generateClass(Filer filer, Messager messager, MockAnnotatedClass mockClass) throws IOException {
        Element typeElement = mockClass.getAnnotatedClassElement();

        String className = typeElement.getSimpleName().toString();
        TypeName classType = TypeName.get(typeElement.asType());


        MethodSpec.Builder builder = MethodSpec.methodBuilder(Helper.generatedMockGetter())
                .returns(classType)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addCode(CodeBlock.builder().build())
                .addStatement("$T $L = new $T()", classType, Helper.OBJECT_NAME, classType);

        for (Element e : typeElement.getEnclosedElements()) {
            if (!e.getKind().isField()) continue;

            String nullOrFunction = nullOfHelperFunction(e);
            if (nullOrFunction == null) {
                String comment = String.format("For field \"%s\" value is set to null.", e.getSimpleName());
                builder.addComment(comment);
                messager.printMessage(Diagnostic.Kind.WARNING, comment);
            }

            builder.addStatement("$L.$L = $L", Helper.OBJECT_NAME, e.getSimpleName(), nullOrFunction);
        }

        builder.addStatement("return $L", Helper.OBJECT_NAME);


        Method annotationMethod = MockTypeHelper.class.getMethods()[0];
        String annotationMethodName = annotationMethod.getName();

        CodeBlock codeBlock = CodeBlock.builder().add("{$L.class}", className).build();
        AnnotationSpec specs = AnnotationSpec.builder(MockTypeHelper.class).addMember(annotationMethodName, codeBlock).build();
        MethodSpec methodGetMock = builder.addAnnotation(specs).build();
        TypeSpec generatedClass = TypeSpec.classBuilder(Helper.generatedMockClassName(className))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(GeneratedMock.class)
                .addMethod(methodGetMock)
                .build();

        JavaFile generatedFile = JavaFile.builder(Helper.GENERATED_PACKAGE, generatedClass).build();

        try {
            generatedFile.writeTo(filer);
        } catch (IOException ioEx) {
            messager.printMessage(Diagnostic.Kind.ERROR, ioEx.toString());
        }
    }

    private static String nullOfHelperFunction(Element field) {
        return MockCompilerProcessor.registeredFunctions.get(field.asType().toString());
    }
}
