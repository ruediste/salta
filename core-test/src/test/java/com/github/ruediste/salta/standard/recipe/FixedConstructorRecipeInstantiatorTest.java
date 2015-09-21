package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.util.Accessibility;

public class FixedConstructorRecipeInstantiatorTest {

    @Before
    public void setup() {
        injector = Salta.createInjector();
        injector.injectMembers(this);
    }

    private static class TestException extends Error {
        private static final long serialVersionUID = 1L;

    }

    private static class TestClassThrow {
        @SuppressWarnings("unused")
        public TestClassThrow() {
            throw new TestException();
        }
    }

    private static class PrivateTestClassPublicConstructor {
        @SuppressWarnings("unused")
        public PrivateTestClassPublicConstructor() {
        }
    }

    public static class PublicTestClassPublicConstructor {
        @SuppressWarnings("unused")
        public PublicTestClassPublicConstructor() {
        }
    }

    private static class TestClassPrivateConstructor {
        private TestClassPrivateConstructor() {
        }
    }

    @Inject
    Provider<TestClassThrow> pThrow;
    private Injector injector;

    @Test
    public void testCatch() {
        try {
            pThrow.get();
        } catch (SaltaException e) {
            if (!(e.getCause() instanceof TestException)) {
                throw e;
            }
        }
    }

    @Test
    public void testPublicConstructorInnerClass() {
        assertNotNull(injector
                .getInstance(PrivateTestClassPublicConstructor.class));
    }

    @Test
    public void testPublicConstructorOuterClass() {
        assertNotNull(injector.getInstance(ArrayList.class));
    }

    @Test
    public void testPrivateConstructor() {
        assertNotNull(injector.getInstance(TestClassPrivateConstructor.class));
    }

    @Test
    public void testIsConstructorAccessible() throws Exception {
        checkIsConstructorAccessible(false,
                PrivateTestClassPublicConstructor.class);
        checkIsConstructorAccessible(true,
                PublicTestClassPublicConstructor.class);
        checkIsConstructorAccessible(false, TestClassPrivateConstructor.class);

    }

    private void checkIsConstructorAccessible(boolean expected, Class<?> clazz)
            throws NoSuchMethodException {
        assertEquals(expected, Accessibility.isConstructorAccessible(
                clazz.getDeclaredConstructor(), getClass().getClassLoader()));
    }
}
