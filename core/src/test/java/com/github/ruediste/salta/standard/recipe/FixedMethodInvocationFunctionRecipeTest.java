package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.io.ByteStreams;

public class FixedMethodInvocationFunctionRecipeTest {

    @Test
    public void assumption_voidClassEqualsVoidReturnType() throws Exception {
        assertTrue(void.class.equals(
                getClass().getMethod("assumption_voidClassEqualsVoidReturnType")
                        .getReturnType()));
    }

    int count;

    void a() {
        count++;
    }

    RecipeCompiler compiler = new RecipeCompiler();

    @Before
    public void before() {
        count = 0;
    }

    @Test
    public void compile_accessToProtectedMethod() throws Exception {
        FixedMethodInvocationFunctionRecipe recipe = new FixedMethodInvocationFunctionRecipe(
                getClass().getDeclaredMethod("a"), Collections.emptyList());
        CompiledSupplier compiled = compiler
                .compileSupplier(new SupplierRecipe() {

                    @Override
                    protected Class<?> compileImpl(GeneratorAdapter mv,
                            MethodCompilationContext ctx) {
                        ctx.addFieldAndLoad(Object.class,
                                FixedMethodInvocationFunctionRecipeTest.this);
                        mv.dup();
                        recipe.compile(Object.class, ctx);
                        return Object.class;
                    }
                });
        count = 0;
        assertSame(this, compiled.getNoThrow());
        assertEquals(1, count);
    }

    private static class LoadSingleClassClassLoader extends ClassLoader {
        private Class<?> clsToLoad;

        LoadSingleClassClassLoader(Class<?> clsToLoad) {
            super(LoadSingleClassClassLoader.class.getClassLoader());
            this.clsToLoad = clsToLoad;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            if (name.startsWith(clsToLoad.getName())) {
                InputStream in = getResourceAsStream(
                        name.replace('.', '/') + ".class");
                try {
                    byte[] bb = ByteStreams.toByteArray(in);
                    return defineClass(name, bb, 0, bb.length);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            return super.loadClass(name, resolve);
        }
    }

    public static class A {

        public int count;

        public void a() {
            count++;
        }
    }

    @Test
    public void compile_targetClassNotFoundByCtxClassLoader() throws Throwable {
        LoadSingleClassClassLoader cl = new LoadSingleClassClassLoader(
                getClass());
        Class<?> clsA = cl.loadClass(A.class.getName());
        FixedMethodInvocationFunctionRecipe recipe = new FixedMethodInvocationFunctionRecipe(
                clsA.getDeclaredMethod("a"), Collections.emptyList());
        Object a = clsA.newInstance();
        CompiledSupplier compiled = compiler
                .compileSupplier(new SupplierRecipe() {

                    @Override
                    protected Class<?> compileImpl(GeneratorAdapter mv,
                            MethodCompilationContext ctx) {
                        ctx.addFieldAndLoad(Object.class, a);
                        mv.dup();
                        recipe.compile(Object.class, ctx);
                        return Object.class;
                    }
                });
        assertSame(a, compiled.getNoThrow());
        assertEquals(1, clsA.getDeclaredField("count").get(a));
    }
}
