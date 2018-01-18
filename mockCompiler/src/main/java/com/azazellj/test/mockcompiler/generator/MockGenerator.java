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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collection;

public class MockGenerator {

    private static void p(Messager messager, String key, String value) {
        messager.printMessage(Diagnostic.Kind.ERROR, key + " " + value);
    }

    public static void generateClass(Elements mElementUtils, Types mTypes, Filer filer, Messager messager,
                                     MockAnnotatedClass mockClass)
            throws IOException {
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


            TypeMirror eC = mTypes.erasure(e.asType());

            Element erasureElem = mTypes.asElement(eC);





            if (erasureElem != null) {
                TypeElement lte = mElementUtils.getTypeElement(Collection.class.getName());

//                p(messager, "erasureElem", erasureElem.toString());

                TypeMirror eCl = mTypes.erasure(lte.asType());


                p(messager, "e.asType() ", e.asType().toString());
                p(messager, "erasureElem.asType() ", erasureElem.asType().toString());
                p(messager, "lte.asType() ", lte.asType().toString());

                p(messager, "is assignable e1 = ", String.valueOf(mTypes.isAssignable(eC, eCl)));
                p(messager, "declared instance = ", String.valueOf(e.asType() instanceof DeclaredType));

                DeclaredType dt = (DeclaredType) e.asType();

                p(messager, "type params = ", String.valueOf(dt.getTypeArguments()));


//                p(messager, "dt = ", String.valueOf(collectionType.toString()));


//                p(messager, "sub type = ", String.valueOf(mTypes.isSubtype(erasureElem.asType(),lte.asType())));
//                p(messager, "same type = ", String.valueOf(mTypes.isSameType(erasureElem.asType(),lte.asType())));
//                p(messager, "is assignable = ", String.valueOf(mTypes.isAssignable(erasureElem.asType(),lte.asType
// ())));
//                p(messager, "is assignable e = ", String.valueOf(mTypes.isAssignable(e.asType(),lte.asType()
//                )));
//
//                p(messager, "hides = ",  String.valueOf(mElementUtils.hides(erasureElem, lte)));
//
//
//
//                p(messager, "lte", lte.toString());
//                p(messager, "lte.getSuperclass", lte.getNestingKind().toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//                p(messager, "lte", lte.toString());
//
//p(messager, "is sub type " + erasureElem.toString() + " of " +lte.toString()+ " = ", String.valueOf( mTypes
//        .isSubtype
//        (lte
//        .asType(), eC)
//) );


//                messager.printMessage(Diagnostic.Kind.ERROR, String.valueOf(mTypes.isAssignable(erasureElem.asType(),
//                        mElementUtils.getTypeElement(List.class.getName()).asType())));
//                messager.printMessage(Diagnostic.Kind.ERROR, String.valueOf(mTypes.isSubtype(erasureElem.asType(),
//                        mElementUtils.getTypeElement(List.class.getName()).asType())));

            }

//            messager.printMessage(Diagnostic.Kind.ERROR,   e.asType().toString());


            String nullOrFunction = nullOfHelperFunction(e);
            if (nullOrFunction == null) {
                String comment = String.format("For field \"%s\" value is set to null.", e.getSimpleName());
                builder.addComment(comment);
                messager.printMessage(Diagnostic.Kind.WARNING, comment);
            }

            builder.addStatement("$L.$L = $L", Helper.OBJECT_NAME, e.getSimpleName(), nullOrFunction);
        }

        builder.addStatement("return $L", Helper.OBJECT_NAME);


//        Method annotationMethod = MockTypeHelper.class.getMethods()[0];
//        String annotationMethodName = annotationMethod.getName();

        CodeBlock codeBlock = CodeBlock.builder().add("{$L.class}", className).build();
        AnnotationSpec specs = AnnotationSpec.builder(MockTypeHelper.class).addMember("types", codeBlock).build();
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
