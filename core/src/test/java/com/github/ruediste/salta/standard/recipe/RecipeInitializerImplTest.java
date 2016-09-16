package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class RecipeInitializerImplTest {

    Object instance;

    @Test
    public void test() throws Throwable {

        RecipeCompiler compiler = new RecipeCompiler();
        Object result = compiler.compileSupplier(new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                mv.push(1);
                return new RecipeInitializerImpl(x -> {
                    instance = x;
                }).compile(int.class, ctx);
            }
        }).get();

        assertEquals(1, result);
        assertEquals(1, instance);
    }
}
