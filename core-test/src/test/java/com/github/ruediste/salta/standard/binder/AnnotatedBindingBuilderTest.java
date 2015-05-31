package com.github.ruediste.salta.standard.binder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.jsr330.util.Names;

public class AnnotatedBindingBuilderTest {

    private static class TestA {
        @Named("foo")
        @Inject
        TestB b;
    }

    private static class TestB {

    }

    private static class TestC {
        @Named("foo")
        @Inject
        TestD d;
    }

    @Named("foo")
    private static class TestD {

    }

    @Test
    public void baseCase() {
        assertNotNull(Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(TestB.class).annotatedWith(Names.named("foo")).to(
                        TestB.class);
            }
        }).getInstance(TestA.class).b);
    }

    @Test
    public void unannotatedClassNotUsedForAnnotated() {
        try {
            Salta.createInjector(new AbstractModule() {

                @Override
                protected void configure() {
                    bind(TestB.class).to(TestB.class);
                }
            }).getInstance(TestA.class);
            fail();
        } catch (SaltaException e) {
            if (!e.getRecursiveCauses().anyMatch(
                    x -> x.getMessage().contains("No recipe found for field")))
                throw e;
        }
    }

    @Test
    public void unannotatedClassNotUsedForAnnotatedJIT() {
        try {
            Salta.createInjector().getInstance(TestA.class);
            fail();
        } catch (SaltaException e) {
            if (!e.getRecursiveCauses().anyMatch(
                    x -> x.getMessage().contains("No recipe found for field")))
                throw e;
        }
    }

    @Test
    public void annotationPresentOnImplementation() {
        Salta.createInjector().getInstance(TestC.class);
    }
}
