package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertNotNull;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCompiler;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Stage;

public class FixedMethodRecipeMembersInjectorTest {
	private Injector injector;

	private static class TestClass {
		@Inject
		public Object m1() {
			return null;
		}

		@Inject
		public Object m2(Stage stage) {
			return null;
		}

		@Inject
		private Object mPrivate(Stage stage) {
			return null;
		}

		@Inject
		Object mPackage(Stage stage) {
			return null;
		}

		@Inject
		protected Object mProtected(Stage stage) {
			return null;
		}

	}

	public static class TestA {
		@Inject
		public Object publicNonVisible(TestB b) {
			return null;
		}

	}

	private static class TestB {
	}

	private RecipeCompiler compiler;

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
		compiler = new RecipeCompiler();
	}

	@Test
	public void testInjectionWorks() {
		injector.getInstance(TestClass.class);
	}

	@Test
	public void testPublicNonVisible() {
		injector.getInstance(TestA.class);
	}

	public static class TestPublic {
		@Inject
		public void foo() {
		}

		@Inject
		public Object withResult() {
			return null;
		}
	}

	@Test
	public void testPublic() {
		injector.getInstance(TestPublic.class);

		TestPublic p = new TestPublic();
		injector.injectMembers(p);
	}

	public static class TestPub {
	}

	private static class TestPriv {
	}

	public static class TestC {
		public void m() {
		}

		public void m(int i) {
		}

		public void m(double i) {
		}

		public void m(Object o) {
		}

		public void m(TestPub t) {
		}

		public void m(TestPriv t) {
		}

		void m1() {
		}

		void m1(int i) {
		}

		void m1(double i) {
		}

		void m1(Object o) {
		}

		void m1(TestPub t) {
		}

		void m1(TestPriv t) {
		}
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

		void m() throws NoSuchMethodException, SecurityException {
			new FixedMethodRecipeMembersInjector(
					TestC.class.getDeclaredMethod(m), Arrays.asList(), strategy);
		}

		void m(Class<?> param, SupplierRecipe recipe)
				throws NoSuchMethodException, SecurityException {
			new FixedMethodRecipeMembersInjector(TestC.class.getDeclaredMethod(
					m, param), Arrays.asList(recipe), strategy).compile(
					TestC.class, ctx);
		}

		SupplierRecipe toObject(SupplierRecipe arg) {
			return new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					arg.compile(ctx);
					mv.visitMethodInsn(
							INVOKESTATIC,
							"com/github/ruediste/salta/standard/recipe/FixedMethodRecipeMembersInjectorTest$Helper",
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
							"com/github/ruediste/salta/standard/recipe/FixedMethodRecipeMembersInjectorTest$Helper",
							"getPub",
							"()Lcom/github/ruediste/salta/standard/recipe/FixedMethodRecipeMembersInjectorTest$TestPub;",
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
							"com/github/ruediste/salta/standard/recipe/FixedMethodRecipeMembersInjectorTest$Helper",
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
				doTestDetail("m", strategy);
			} catch (Throwable t) {
				throw new RuntimeException("Error in public/" + strategy, t);
			}
			try {
				doTestDetail("m1", strategy);
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

				ctx.addFieldAndLoad(TestC.class, new TestC());
				try {
					// void
					b.m();

					// int
					b.m(int.class, b.getInteger());
					b.m(int.class, b.getIntP());
					b.m(int.class, b.toObject(b.getInteger()));

					// double
					b.m(double.class, b.getDouble());
					b.m(double.class, b.getDoubleP());
					b.m(double.class, b.toObject(b.getDouble()));

					// Object
					b.m(Object.class, b.toObject(b.getInteger()));
					b.m(Object.class, b.getIntP());
					b.m(Object.class, b.getDoubleP());

					// Pub
					b.m(TestPub.class, b.getPub());
					b.m(TestPub.class, b.toObject(b.getPub()));

					// Priv
					b.m(TestPriv.class, b.getPriv());
				} catch (NoSuchMethodException | SecurityException e) {
					throw new SaltaException();
				}

				// mv.pop();
				return TestC.class;
			}

		};

		assertNotNull(compiler.compileSupplier(recipe).get());
	}
}
