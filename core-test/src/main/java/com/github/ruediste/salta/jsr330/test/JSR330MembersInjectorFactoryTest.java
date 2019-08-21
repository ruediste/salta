package com.github.ruediste.salta.jsr330.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;

public class JSR330MembersInjectorFactoryTest {

    public static class TestClassA {
        private TestClassB b;
        private TestClassB b1;
        private String c;

        @Inject
        void b(TestClassB b) {
            this.b = b;
        }

        void b1(TestClassB b1) {
            this.b1 = b1;
        }

        @Named("foo")
        void c(String c) {
            this.c = c;
        }

    }

    public static class TestClassB {

    }

    private Injector injector;

    @Before
    public void before() {
        injector = Salta.createInjector();
    }

    @Test
    public void testInjectInjected() {
        TestClassA a = injector.getInstance(TestClassA.class);
        assertNotNull(a.b);
    }

    @Test
    public void testNoInjectAnnotation() {
        TestClassA a = injector.getInstance(TestClassA.class);
        assertNull(a.b1);
        assertNull(a.c);
    }

}
