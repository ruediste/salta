package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.util.Names;
import com.github.ruediste.salta.standard.DependencyKey;

public class ProvidesMethodTest {

    private static class A {
        @Inject
        @Named("fails")
        String other;
    }

    @Test
    public void exceptionIsWrapped() {
        Injector injector = Salta.createInjector(new AbstractModule() {

            @Override
            protected void configure() {

            }

            @Provides
            @Named("fails")
            String create() {
                throw new RuntimeException("boo");
            }

            @Provides
            @Named("failsChecked")
            String createFailChecked() throws Exception {
                throw new Exception("booChecked");
            }

            @Provides
            @Named("works")
            String createWorks() {
                return "Hello World";
            }

        });
        assertEquals("Hello World", injector.getInstance(DependencyKey.of(
                String.class).withAnnotations(Names.named("works"))));

        try {
            injector.getInstance(DependencyKey.of(String.class)
                    .withAnnotations(Names.named("fails")));
            fail();
        } catch (SaltaException e) {
            assertTrue(e.getMessage().contains("boo"));
        }
        try {
            injector.getInstance(DependencyKey.of(String.class)
                    .withAnnotations(Names.named("failsChecked")));
            fail();
        } catch (SaltaException e) {
            assertTrue(e.getMessage().contains("booChecked"));
        }
        try {
            injector.getInstance(A.class);
            fail();
        } catch (SaltaException e) {
            assertTrue(e.getMessage().contains("boo"));
        }
    }
}
