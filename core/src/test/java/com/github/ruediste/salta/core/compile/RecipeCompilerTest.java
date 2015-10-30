package com.github.ruediste.salta.core.compile;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

public class RecipeCompilerTest {

    private RecipeCompiler compiler;

    @Before
    public void setup() {
        compiler = new RecipeCompiler();
    }

    @Test
    public void testObjectSupplier() throws Throwable {
        SupplierRecipe recipe = new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv,
                    MethodCompilationContext ctx) {
                mv.visitInsn(Opcodes.ICONST_3);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                        "(I)Ljava/lang/Integer;", false);
                return Integer.class;
            }

        };

        assertEquals(3, compiler.compileSupplier(recipe).get());
    }

    @Test
    public void testPrimitiveSupplier() throws Throwable {
        SupplierRecipe recipe = new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv,
                    MethodCompilationContext ctx) {
                mv.visitInsn(Opcodes.ICONST_3);
                return int.class;
            }

        };

        assertEquals(3, compiler.compileSupplier(recipe).get());
    }

    @Test
    public void testObjectFunction() throws Throwable {
        FunctionRecipe recipe = new FunctionRecipe() {

            @Override
            public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
                    MethodCompilationContext ctx) {
                mv.pop();
                mv.visitInsn(Opcodes.ICONST_3);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                        "(I)Ljava/lang/Integer;", false);
                return Integer.class;
            }

        };

        assertEquals(3, compiler.compileFunction(recipe).get(5));
    }

    @Test
    public void testPrimitiveFunction() throws Throwable {
        FunctionRecipe recipe = new FunctionRecipe() {

            @Override
            public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
                    MethodCompilationContext ctx) {
                mv.pop();
                mv.visitInsn(Opcodes.ICONST_3);
                return int.class;
            }

        };

        assertEquals(3, compiler.compileFunction(recipe).get(5));
    }

}
