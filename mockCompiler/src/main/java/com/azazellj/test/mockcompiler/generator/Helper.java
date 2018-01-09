package com.azazellj.test.mockcompiler.generator;

public class Helper {
    private Helper() {}

    public static final String GENERATED_PACKAGE = Helper.class.getPackage().getName();
    public static final String OBJECT_NAME = "someObject";


    public static String generatedMockClassName(String className) {
        return "Mock" + className;
    }

    public static String generatedMockGetter() {
        return MockObject.class.getMethods()[0].getName();
    }

    public static String fullGeneratedHelperPath(String className) {
        return GENERATED_PACKAGE + "." + generatedMockClassName(className) + "." + generatedMockGetter() + "()";
    }
}
