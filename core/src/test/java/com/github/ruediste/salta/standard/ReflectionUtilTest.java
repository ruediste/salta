package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReflectionUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    class Inner {
    }

    @Test
    public void testGetPackageNameClass() throws Exception {
        assertEquals("com.github.ruediste.salta.standard", ReflectionUtil.getPackageName(ReflectionUtilTest.class));
        assertEquals("com.github.ruediste.salta.standard", ReflectionUtil.getPackageName(Inner.class));
    }

}
