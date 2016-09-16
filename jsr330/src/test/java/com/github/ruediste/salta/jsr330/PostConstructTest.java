package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;

import javax.annotation.PostConstruct;

import org.junit.Test;

public class PostConstructTest {

    private static class TestClass {
        int initializeCount;

        @PostConstruct
        void init() {
            initializeCount++;
        }
    }

    private static class TestClassDerived extends TestClass {

        @Override
        @PostConstruct
        void init() {
            initializeCount++;
        }
    }

    @Test
    public void testPostConstructCalled() {
        assertEquals("initialization called once", 1,
                Salta.createInjector().getInstance(TestClass.class).initializeCount);
    }

    @Test
    public void testPostConstructCalledOnDerived() {
        assertEquals("initialization called once", 1,
                Salta.createInjector().getInstance(TestClassDerived.class).initializeCount);
    }
}
