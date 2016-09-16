package com.github.ruediste.salta.core.compile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class SupplierRecipeTest {

    public static class TestClass {
        String innerName;
        String outerName;

        public TestClass(String innerName, String outerName) {
            super();
            this.innerName = innerName;
            this.outerName = outerName;
        }

    }

    @Test
    public void testMethodSplit() throws Throwable {
        SupplierRecipe innerNameRecipe = new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {

                ctx.addFieldAndLoad(String.class, ctx.getClassCtx().getInternalClassName());
                return String.class;
            }
        };
        SupplierRecipe recipe = new SupplierRecipe(3) {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                mv.newInstance(Type.getType(TestClass.class));
                mv.dup();
                Class<?> t = innerNameRecipe.compile(ctx);
                ctx.castToPublic(t, String.class);
                ctx.addFieldAndLoad(String.class, ctx.getClassCtx().getInternalClassName());
                mv.invokeConstructor(Type.getType(TestClass.class), Method.getMethod("void <init>(String, String)"));
                // mv.pop();
                return TestClass.class;
            }
        };
        TestClass test = (TestClass) new RecipeCompiler().compileSupplier(recipe).get();
        assertTrue("expected different classes", !test.innerName.equals(test.outerName));
    }
}
