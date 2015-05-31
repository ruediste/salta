package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.jsr330.JSR330ReflectionUtil;

public class JSR330ReflectionUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Qualifier
    @java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
    private @interface TestQualifier {

    }

    private static class TestClass {
        @Named("foo")
        public int a;

        @Inject
        @TestQualifier
        public int b;

        @SuppressWarnings("unused")
        @Inject
        public int c;
    }

    @Test
    public void getQualifiers() throws Exception {
        assertEquals(
                1,
                JSR330ReflectionUtil.getQualifiers(
                        TestClass.class.getField("a")).size());
        assertEquals(
                1,
                JSR330ReflectionUtil.getQualifiers(
                        TestClass.class.getField("b")).size());
        assertEquals(
                0,
                JSR330ReflectionUtil.getQualifiers(
                        TestClass.class.getField("c")).size());
    }
}
