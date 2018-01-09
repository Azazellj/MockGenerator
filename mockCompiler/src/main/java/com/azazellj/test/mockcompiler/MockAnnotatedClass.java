package com.azazellj.test.mockcompiler;

import com.azazellj.mock.annotations.Mock;

import javax.lang.model.element.Element;

public class MockAnnotatedClass {
    private Element annotatedClassElement;

    private Class[] ignoredTypes;

    public MockAnnotatedClass(Element classElement) {
        this.annotatedClassElement = classElement;
        Mock annotation = classElement.getAnnotation(Mock.class);
//        ignoredTypes = annotation.ignoredTypes();
    }

    public Element getAnnotatedClassElement() {
        return annotatedClassElement;
    }

    public Class[] getIgnoredTypes() {
        return ignoredTypes;
    }
}
