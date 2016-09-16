package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class RecipeEnhancerWrapperImplTest {

    Object supplier;
    Object instance;

    @Test
    public void test() throws Throwable {

        RecipeCompiler compiler = new RecipeCompiler();
        Object result = compiler.compileSupplier(new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                return new RecipeEnhancerWrapperImpl(new Function<Supplier<Object>, Object>() {

                    @Override
                    public Object apply(Supplier<Object> t) {
                        assertNull(supplier);
                        supplier = t;
                        instance = t.get();
                        return "Hello";
                    }
                }).compile(ctx, new SupplierRecipe() {

                    @Override
                    protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                        mv.push(1);
                        return int.class;
                    }
                });
            }
        }).get();

        assertEquals("Hello", result);
        assertNotNull(supplier);
        assertEquals(1, instance);
    }
}
