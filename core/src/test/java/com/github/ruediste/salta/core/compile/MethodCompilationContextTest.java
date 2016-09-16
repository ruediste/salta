package com.github.ruediste.salta.core.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.standard.util.Accessibility;

public class MethodCompilationContextTest {

    private RecipeCompiler compiler;

    @Before
    public void setup() {
        compiler = new RecipeCompiler();
    }

    public static class CastTestHelper {
        public static void acceptInteger(Integer i) {
            assertEquals(Integer.valueOf(4), i);
        }

        public static void acceptInt(int i) {
            assertEquals(4, i);
        }

        public static void acceptObject(Object o) {
        }

        public static Object toObject(Object o) {
            return o;
        }

        public static Integer toInteger(Integer o) {
            return o;
        }

        public static void acceptArray(Object[] array) {

        }
    }

    @Test
    public void testCastToPublic() throws Throwable {
        FunctionRecipe recipe = new FunctionRecipe() {

            @Override
            public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv, MethodCompilationContext ctx) {
                // primitive to boxed
                mv.push(4);
                ctx.castToPublic(int.class, Integer.class);
                acceptInteger(mv);

                // primitive to Object
                mv.push(4);
                ctx.castToPublic(int.class, Object.class);
                acceptInteger(mv);

                // Object to primitive
                mv.push(4);
                ctx.castToPublic(int.class, Integer.class);
                toObject(mv);
                ctx.castToPublic(Object.class, int.class);
                acceptInt(mv);

                // boxed to primitive
                mv.push(4);
                ctx.castToPublic(int.class, Integer.class);
                toInteger(mv);
                ctx.castToPublic(Integer.class, int.class);
                acceptInt(mv);

                // down cast
                mv.push(4);
                ctx.castToPublic(int.class, Integer.class);
                toObject(mv);
                ctx.castToPublic(Object.class, Integer.class);
                acceptInteger(mv);

                // object to array
                mv.push(0);
                mv.newArray(Type.getType(Object.class));
                ctx.castToPublic(Object[].class, Object.class);
                toObject(mv);
                ctx.castToPublic(Object.class, Object[].class);
                acceptArray(mv);

                mv.push(4);
                return int.class;
            }

            protected void acceptArray(GeneratorAdapter mv) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(CastTestHelper.class), "acceptArray",
                        "([Ljava/lang/Object;)V", false);
            }

            protected void toInteger(GeneratorAdapter mv) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(CastTestHelper.class), "toInteger",
                        "(Ljava/lang/Integer;)Ljava/lang/Integer;", false);
            }

            protected void acceptInt(GeneratorAdapter mv) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(CastTestHelper.class), "acceptInt", "(I)V",
                        false);
            }

            protected void toObject(GeneratorAdapter mv) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(CastTestHelper.class), "toObject",
                        "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            }

            protected void acceptInteger(GeneratorAdapter mv) {
                mv.visitMethodInsn(INVOKESTATIC,

                Type.getInternalName(CastTestHelper.class), "acceptInteger", "(Ljava/lang/Integer;)V", false);
            }

        };

        assertEquals(4, compiler.compileFunction(recipe).get(5));
    }

    @Test
    public void assumptions() {
        assertTrue(Accessibility.isClassAccessible(int.class, getClass().getClassLoader()));
        assertFalse(Object.class.isAssignableFrom(int.class));
    }

    @Test
    public void testCompileToSupplier() throws Throwable {
        Object result = compiler.compileSupplier(new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                ctx.compileToSupplier(new SupplierRecipe() {

                    @Override
                    protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                        mv.push(1);
                        return int.class;
                    }
                });
                mv.invokeInterface(Type.getType(Supplier.class), Method.getMethod("Object get()"));
                return Object.class;
            }
        }).get();
        assertEquals(1, result);
    }
}
