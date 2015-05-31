package com.github.ruediste.salta.core;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.jsr330.Salta;

public class DependencyCircleTest {

    private Injector injector;

    static class ConstructorTestClassA {
        public ConstructorTestClassB b;

        ConstructorTestClassA() {
        }

        @Inject
        ConstructorTestClassA(ConstructorTestClassB b) {
            this.b = b;

        }
    }

    static class ConstructorTestClassB {
        public ConstructorTestClassA a;

        @Inject
        ConstructorTestClassB(ConstructorTestClassA a) {
            this.a = a;

        }
    }

    static class FieldTestClassA {
        @Inject
        public FieldTestClassB b;

        @Inject
        FieldTestClassA() {
        }
    }

    static class FieldTestClassB {
        @Inject
        public FieldTestClassA a;

        @Inject
        FieldTestClassB() {
        }
    }

    @Before
    public void setup() {
        injector = Salta.createInjector();
    }

    @Test(expected = SaltaException.class)
    public void circularConstructorTest() {
        injector.getInstance(ConstructorTestClassA.class);
    }

    @Test(expected = SaltaException.class)
    public void circularFieldTest() {
        injector.getInstance(FieldTestClassA.class);
    }
}
