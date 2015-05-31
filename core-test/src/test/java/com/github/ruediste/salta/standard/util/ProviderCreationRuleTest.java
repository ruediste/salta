package com.github.ruediste.salta.standard.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.InjectionOptional;
import com.github.ruediste.salta.jsr330.Salta;

public class ProviderCreationRuleTest {

    private static class A {
        @PostConstruct
        void initialize(Provider<B> b) {

        }
    }

    private static class B {
        /**
         * Constructor is not cannot be injected, would need @Inject annotation
         */
        @SuppressWarnings("unused")
        public B(String canNotInject) {

        }
    }

    private static class C {
        private Provider<B> b;
        @Inject
        @InjectionOptional
        Provider<B> b1;

        private Provider<B> b2;

        @Inject
        C(@InjectionOptional Provider<B> b2) {
            this.b2 = b2;
        }

        @PostConstruct
        void initialize(@InjectionOptional Provider<B> b) {
            this.b = b;
        }
    }

    @Test
    public void testNotPresentProvider() {
        try {
            Salta.createInjector().getInstance(A.class);
        } catch (SaltaException e) {
            if (!e.getMessage().contains(
                    "Cannot resolve initializer parameter of"))
                throw e;
        }
    }

    @Test
    public void testOptionalProvider() {
        C c = Salta.createInjector().getInstance(C.class);
        assertNotNull(c);
        assertNull(c.b);
        assertNull(c.b1);
        assertNull(c.b2);
    }
}
