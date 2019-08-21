package com.github.ruediste.salta.jsr330.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;

public class SimpleTest {

    @Test
    public void emptyInjector() {
        Injector injector = Salta.createInjector();
        assertNotNull(injector);
    }
}
