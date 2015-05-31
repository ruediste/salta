package com.github.ruediste.salta.jsr330.wikiChecks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import javax.inject.Singleton;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.DefaultScope;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Provides;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.Stage;

public class ScopesTest {

    private interface IA {
    }

    @Singleton
    private static class A implements IA {
    }

    @Test
    public void scopeOnClass() {
        Injector injector = Salta.createInjector();
        assertSame(injector.getInstance(A.class), injector.getInstance(A.class));
    }

    @Test
    public void scopeOnClassBound() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(A.class).in(DefaultScope.class);
            }
        });
        assertNotSame(injector.getInstance(A.class),
                injector.getInstance(A.class));
    }

    @Test
    public void scopeOverride() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(IA.class).to(A.class);
            }
        });
        assertSame(injector.getInstance(IA.class),
                injector.getInstance(IA.class));
    }

    private interface IB {
    }

    private static class B implements IB {
    }

    @Test
    public void scopeDefinedInBinding() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(B.class).in(Singleton.class);
            }
        });
        assertSame(injector.getInstance(B.class), injector.getInstance(B.class));
    }

    @Test
    public void scopeDefinedInProvides() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
            }

            @Provides
            @Singleton
            IB theB() {
                return new B();
            }
        });
        assertSame(injector.getInstance(IB.class),
                injector.getInstance(IB.class));
    }

    private interface Bar {
    }

    private interface Grill {
    }

    private static class Applebees implements Bar, Grill {
    }

    @Test
    public void testTwoInstances() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(Bar.class).to(Applebees.class).in(Singleton.class);
                bind(Grill.class).to(Applebees.class).in(Singleton.class);
            }
        });
        assertNotSame(injector.getInstance(Bar.class),
                injector.getInstance(Grill.class));
    }

    @Test
    public void testSingleInstance() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(Applebees.class).in(Singleton.class);
            }

            @Provides
            Bar ic1(Applebees c) {
                return c;
            }

            @Provides
            Grill ic2(Applebees c) {
                return c;
            }
        });
        assertSame(injector.getInstance(Bar.class),
                injector.getInstance(Grill.class));
    }

    private static class C {
        static int count;

        C() {
            count++;
        }
    }

    @Test
    public void eagerSingleton() {
        C.count = 0;
        Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(C.class).asEagerSingleton();
            }
        });
        assertEquals(1, C.count);

        C.count = 0;
        Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(C.class).in(Singleton.class);
            }
        });
        assertEquals(0, C.count);

        C.count = 0;
        Salta.createInjector(Stage.PRODUCTION, new AbstractModule() {

            @Override
            protected void configure() throws Exception {
                bind(C.class).in(Singleton.class);
            }
        });
        assertEquals(1, C.count);
    }

}
