package com.github.ruediste.salta.jsr330.test;

import static org.junit.Assert.assertNotSame;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.util.ProviderCreationRule.ProviderAccessBeforeInstanceCreationFinishedException;

public class ProviderDependencyFactoryRuleTest {

    private Injector injector;

    private static class TestClassA {
        private TestClassB b;

        @Inject
        public TestClassA(TestClassB b) {
            this.b = b;
        }
    }

    private static class TestClassB {
        private Provider<TestClassA> aProvider;

        @Inject
        public TestClassB(Provider<TestClassA> aProvider) {
            this.aProvider = aProvider;
        }
    }

    private static class TestClassC {
        @Inject
        public TestClassC(TestClassD d) {
        }
    }

    private static class TestClassD {
        @Inject
        public TestClassD(Provider<TestClassC> cProvider) {
            // access in constructor -> forbidden
            cProvider.get();
        }
    }

    @Singleton
    private static class TestClassE {
        @Inject
        public TestClassE(Provider<TestClassE> p) {
            p.get();
        }
    }

    @Before
    public void setup() {
        injector = Salta.createInjector();
    }

    @Test
    public void testInstantiation() {
        TestClassA a = injector.getInstance(TestClassA.class);
        TestClassA otherA = a.b.aProvider.get();
        assertNotSame(otherA, a);
    }

    @Test
    public void testDifferentValues() {
        TestClassA a = injector.getInstance(TestClassA.class);
        TestClassA otherA1 = a.b.aProvider.get();
        TestClassA otherA2 = a.b.aProvider.get();
        assertNotSame(otherA1, otherA2);
    }

    @Test
    public void testForbiddenProviderAccess() {
        try {
            injector.getInstance(TestClassC.class);
        } catch (SaltaException e) {
            if (!(e.getCause() instanceof ProviderAccessBeforeInstanceCreationFinishedException))
                throw e;
        }
    }

    @Test
    public void testForbiddenProviderAccessSingleton() {
        try {
            injector.getInstance(TestClassE.class);
        } catch (SaltaException e) {
            if (!(e.getRecursiveCauses()
                    .anyMatch(x -> x instanceof ProviderAccessBeforeInstanceCreationFinishedException)))
                throw e;
        }

    }
}
