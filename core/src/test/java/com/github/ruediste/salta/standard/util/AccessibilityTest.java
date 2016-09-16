package com.github.ruediste.salta.standard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AccessibilityTest {

    @Test
    public void primitivesArePublic() {
        assertEquals(true, Accessibility.isClassAccessible(int.class, getClass().getClassLoader()));
    }

    @Test
    public void ispublic_arrayWithPublicElements_isAccessible() {
        assertTrue(Accessibility.isClassAccessible(int[].class, getClass().getClassLoader()));
    }

    private class A {
    }

    @Test
    public void ispublic_arrayWithPrivateElements_isNotAccessible() {
        assertFalse(Accessibility.isClassAccessible(A[].class, getClass().getClassLoader()));
    }
}
