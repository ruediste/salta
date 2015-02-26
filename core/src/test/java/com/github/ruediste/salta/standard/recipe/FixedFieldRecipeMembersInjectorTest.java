package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertNotNull;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCompiler;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.SupplierRecipe;

public class FixedFieldRecipeMembersInjectorTest {
	private RecipeCompiler compiler;

	@Before
	public void setup() {
		compiler = new RecipeCompiler();
	}

	public static class TestPub {
	}

	private static class TestPriv {
	}

	public static class TestMain {
		public int f_int;
		int f1_int;
		public Integer f_Integer;
		Integer f1_Integer;
		public double f_double;
		double f1_double;
		public Object f_Object;
		Object f1_Object;
		public TestPub f_TestPub;
		TestPub f1_TestPub;
		public TestPriv f_TestPriv;
		TestPriv f1_TestPriv;
	}

	public static class Helper {
		public static Object toObject(Object o) {
			return o;
		}

		public static TestPub getPub() {
			return new TestPub();
		}

		public static Object getPriv() {
			return new TestPriv();
		}

	}

	private static class Builder {
		private GeneratorAdapter mv;
		private InjectionStrategy strategy;
		String m;
		private RecipeCompilationContext ctx;

		public Builder(GeneratorAdapter mv, RecipeCompilationContext ctx,
				String m, InjectionStrategy strategy) {
			this.mv = mv;
			this.ctx = ctx;
			this.m = m;
			this.strategy = strategy;
		}

		void f(Class<?> param, SupplierRecipe recipe) throws Exception {
			new FixedFieldRecipeMembersInjector(
					TestMain.class.getDeclaredField(m + "_"
							+ param.getSimpleName()), recipe, strategy)
					.compile(TestMain.class, ctx);
		}

		SupplierRecipe toObject(SupplierRecipe arg) {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					arg.compile(ctx);
					mv.visitMethodInsn(
							INVOKESTATIC,
							"com/github/ruediste/salta/standard/recipe/FixedFieldRecipeMembersInjectorTest$Helper",
							"toObject",
							"(Ljava/lang/Object;)Ljava/lang/Object;", false);
					return Object.class;
				}
			};

		}

		SupplierRecipe getPub() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.visitMethodInsn(
							INVOKESTATIC,
							"com/github/ruediste/salta/standard/recipe/FixedFieldRecipeMembersInjectorTest$Helper",
							"getPub",
							"()Lcom/github/ruediste/salta/standard/recipe/FixedFieldRecipeMembersInjectorTest$TestPub;",
							false);

					return TestPub.class;
				}
			};
		}

		SupplierRecipe getPriv() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.visitMethodInsn(
							INVOKESTATIC,
							"com/github/ruediste/salta/standard/recipe/FixedFieldRecipeMembersInjectorTest$Helper",
							"getPriv", "()Ljava/lang/Object;", false);

					return TestPriv.class;
				}
			};
		}

		SupplierRecipe getIntP() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.push(1);
					return int.class;
				}
			};
		}

		SupplierRecipe getInteger() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.push(1);
					mv.box(Type.getType(int.class));
					return Integer.class;
				}
			};
		}

		SupplierRecipe getDoubleP() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.push(1.2);
					return double.class;
				}
			};
		}

		SupplierRecipe getDouble() {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					mv.push(1.2);
					mv.box(Type.getType(double.class));
					return Double.class;
				}
			};
		}

	}

	@Test
	public void testDetail() throws Throwable {
		for (InjectionStrategy strategy : InjectionStrategy.values()) {
			try {
				doTestDetail("f", strategy);
			} catch (Throwable t) {
				throw new RuntimeException("Error in public/" + strategy, t);
			}
			try {
				doTestDetail("f1", strategy);
			} catch (Throwable t) {
				throw new RuntimeException("Error in package/" + strategy, t);
			}
		}
	}

	private void doTestDetail(String m, InjectionStrategy strategy)
			throws Throwable {
		SupplierRecipe recipe = new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					RecipeCompilationContext ctx) {

				Builder b = new Builder(mv, ctx, m, strategy);

				ctx.addFieldAndLoad(TestMain.class, new TestMain());
				try {
					// int
					b.f(int.class, b.getInteger());
					b.f(int.class, b.getIntP());
					b.f(int.class, b.toObject(b.getInteger()));

					// Integer
					b.f(Integer.class, b.getInteger());
					b.f(Integer.class, b.getIntP());
					b.f(Integer.class, b.toObject(b.getInteger()));

					// double
					b.f(double.class, b.getDouble());
					b.f(double.class, b.getDoubleP());
					b.f(double.class, b.toObject(b.getDouble()));

					// Object
					b.f(Object.class, b.toObject(b.getInteger()));
					b.f(Object.class, b.getIntP());
					b.f(Object.class, b.getDoubleP());

					// Pub
					b.f(TestPub.class, b.getPub());
					b.f(TestPub.class, b.toObject(b.getPub()));

					// Priv
					b.f(TestPriv.class, b.getPriv());
				} catch (Exception e) {
					throw new SaltaException(e);
				}

				// mv.pop();
				return TestMain.class;
			}

		};

		assertNotNull(compiler.compileSupplier(recipe).get());
	}
}
