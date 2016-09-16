package com.github.ruediste.salta.standard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.github.ruediste.simpledi.standard.util.testPackageA.TestClassA;
import com.github.ruediste.simpledi.standard.util.testPackageA.TestClassC;
import com.github.ruediste.simpledi.standard.util.testPackageB.TestClassB;
import com.github.ruediste.simpledi.standard.util.testPackageB.TestClassD;

public class MethodOverrideIndexTest {

    private abstract class MyTestA {
        abstract void a();

        abstract void b();

        abstract void d(int i);
    }

    private abstract class MyTestB extends MyTestA {
        @Override
        abstract void b();

        abstract void c();

        abstract void d(long i);
    }

    @Test
    public void testSimple() throws NoSuchMethodException, SecurityException {
        MethodOverrideIndex idx = new MethodOverrideIndex(MyTestB.class);
        assertFalse(idx.isOverridden(MyTestA.class.getDeclaredMethod("a")));
        assertTrue(idx.isOverridden(MyTestA.class.getDeclaredMethod("b")));
        assertFalse(idx.isOverridden(MyTestA.class.getDeclaredMethod("d", int.class)));

        assertFalse(idx.isOverridden(MyTestB.class.getDeclaredMethod("b")));
        assertFalse(idx.isOverridden(MyTestB.class.getDeclaredMethod("c")));
        assertFalse(idx.isOverridden(MyTestB.class.getDeclaredMethod("d", long.class)));
    }

    @Test
    public void testPackages() throws NoSuchMethodException, SecurityException {
        MethodOverrideIndex idx = new MethodOverrideIndex(TestClassD.class);

        assertFalse(idx.isOverridden(TestClassA.class.getDeclaredMethod("a")));
        assertFalse(idx.isOverridden(TestClassA.class.getDeclaredMethod("ab")));
        assertTrue(idx.isOverridden(TestClassA.class.getDeclaredMethod("ac")));
        assertFalse(idx.isOverridden(TestClassA.class.getDeclaredMethod("ad")));

        assertFalse(idx.isOverridden(TestClassB.class.getDeclaredMethod("ab")));
        assertFalse(idx.isOverridden(TestClassB.class.getDeclaredMethod("b")));
        assertFalse(idx.isOverridden(TestClassB.class.getDeclaredMethod("bc")));
        assertTrue(idx.isOverridden(TestClassB.class.getDeclaredMethod("bd")));

        assertFalse(idx.isOverridden(TestClassC.class.getDeclaredMethod("ac")));
        assertFalse(idx.isOverridden(TestClassC.class.getDeclaredMethod("bc")));
        assertFalse(idx.isOverridden(TestClassC.class.getDeclaredMethod("c")));
        assertFalse(idx.isOverridden(TestClassC.class.getDeclaredMethod("cd")));

        assertFalse(idx.isOverridden(TestClassD.class.getDeclaredMethod("ad")));
        assertFalse(idx.isOverridden(TestClassD.class.getDeclaredMethod("bd")));
        assertFalse(idx.isOverridden(TestClassD.class.getDeclaredMethod("cd")));
        assertFalse(idx.isOverridden(TestClassD.class.getDeclaredMethod("d")));
    }

    @Test
    public void testGetOverridingMethods() throws Exception {
        MethodOverrideIndex idx = new MethodOverrideIndex(MyTestB.class);
        assertEquals(0, idx.getOverridingMethods(MyTestA.class.getDeclaredMethod("a")).size());
        assertEquals(1, idx.getOverridingMethods(MyTestA.class.getDeclaredMethod("b")).size());
    }

    @SuppressWarnings("unused")
    private static class BBase<T> {

        void set(T value) {
        }

        void foo(List<String> arg) {
        }
    }

    private static class BDerived extends BBase<Integer> {
        @Override
        void set(Integer value) {
        }

        @Override
        void foo(@SuppressWarnings("rawtypes") List arg) {
        }
    }

    @Test
    public void testGenericOverride() throws Exception {
        MethodOverrideIndex idx = new MethodOverrideIndex(BDerived.class);
        assertTrue(idx.isOverridden(BBase.class.getDeclaredMethod("set", Object.class)));
    }

    @Test
    public void testSubclassRawOverride() throws Exception {
        MethodOverrideIndex idx = new MethodOverrideIndex(BDerived.class);
        assertTrue(idx.isOverridden(BBase.class.getDeclaredMethod("foo", List.class)));
    }
}
