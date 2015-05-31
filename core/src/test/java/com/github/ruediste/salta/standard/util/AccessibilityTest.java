package com.github.ruediste.salta.standard.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccessibilityTest {

    @Test
    public void primitivesArePublic() {
        assertEquals(true, Accessibility.isClassPublic(int.class));
    }
}
