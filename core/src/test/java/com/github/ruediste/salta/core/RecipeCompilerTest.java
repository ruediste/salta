package com.github.ruediste.salta.core;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
					RecipeCompilationContext ctx) {
				mv.visitInsn(Opcodes.ICONST_3);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)Ljava/lang/Integer;", false);
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
					RecipeCompilationContext ctx) {
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
			protected Class<?> compileImpl(Class<?> argType,
					GeneratorAdapter mv, RecipeCompilationContext ctx) {
				mv.pop();
				mv.visitInsn(Opcodes.ICONST_3);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)Ljava/lang/Integer;", false);
				return Integer.class;
			}

		};

		assertEquals(3, compiler.compileFunction(recipe).get(5));
	}

	@Test
	public void testPrimitiveFunction() throws Throwable {
		FunctionRecipe recipe = new FunctionRecipe() {

			@Override
			protected Class<?> compileImpl(Class<?> argType,
					GeneratorAdapter mv, RecipeCompilationContext ctx) {
				mv.pop();
				mv.visitInsn(Opcodes.ICONST_3);
				return int.class;
			}

		};

		assertEquals(3, compiler.compileFunction(recipe).get(5));
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
	public void testCast() throws Throwable {
		FunctionRecipe recipe = new FunctionRecipe() {

			@Override
			protected Class<?> compileImpl(Class<?> argType,
					GeneratorAdapter mv, RecipeCompilationContext ctx) {
				// primitive to boxed
				mv.push(4);
				ctx.cast(int.class, Integer.class);
				acceptInteger(mv);

				// primitive to Object
				mv.push(4);
				ctx.cast(int.class, Object.class);
				acceptInteger(mv);

				// Object to primitive
				mv.push(4);
				ctx.cast(int.class, Integer.class);
				toObject(mv);
				ctx.cast(Object.class, int.class);
				acceptInt(mv);

				// boxed to primitive
				mv.push(4);
				ctx.cast(int.class, Integer.class);
				toInteger(mv);
				ctx.cast(Integer.class, int.class);
				acceptInt(mv);

				// down cast
				mv.push(4);
				ctx.cast(int.class, Integer.class);
				toObject(mv);
				ctx.cast(Object.class, Integer.class);
				acceptInteger(mv);

				// object to array
				mv.push(0);
				mv.newArray(Type.getType(Object.class));
				ctx.cast(Object[].class, Object.class);
				toObject(mv);
				ctx.cast(Object.class, Object[].class);
				acceptArray(mv);

				mv.push(4);
				return int.class;
			}

			protected void acceptArray(GeneratorAdapter mv) {
				mv.visitMethodInsn(
						INVOKESTATIC,
						"com/github/ruediste/salta/core/RecipeCompilerTest$CastTestHelper",
						"acceptArray", "([Ljava/lang/Object;)V", false);
			}

			protected void toInteger(GeneratorAdapter mv) {
				mv.visitMethodInsn(
						INVOKESTATIC,
						"com/github/ruediste/salta/core/RecipeCompilerTest$CastTestHelper",
						"toInteger",
						"(Ljava/lang/Integer;)Ljava/lang/Integer;", false);
			}

			protected void acceptInt(GeneratorAdapter mv) {
				mv.visitMethodInsn(
						INVOKESTATIC,
						"com/github/ruediste/salta/core/RecipeCompilerTest$CastTestHelper",
						"acceptInt", "(I)V", false);
			}

			protected void toObject(GeneratorAdapter mv) {
				mv.visitMethodInsn(
						INVOKESTATIC,
						"com/github/ruediste/salta/core/RecipeCompilerTest$CastTestHelper",
						"toObject", "(Ljava/lang/Object;)Ljava/lang/Object;",
						false);
			}

			protected void acceptInteger(GeneratorAdapter mv) {
				mv.visitMethodInsn(
						INVOKESTATIC,
						"com/github/ruediste/salta/core/RecipeCompilerTest$CastTestHelper",
						"acceptInteger", "(Ljava/lang/Integer;)V", false);
			}

		};

		assertEquals(4, compiler.compileFunction(recipe).get(5));
	}
}
