package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancerBeforeAfterImpl.BeforeAfterEnhancer;

public class RecipeEnhancerBeforeAfterImplTest {

	boolean beforeSeen;
	Object afterSeen;

	@Test
	public void test() throws Throwable {

		RecipeCompiler compiler = new RecipeCompiler();
		Object result = compiler.compileSupplier(new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {
				return new RecipeEnhancerBeforeAfterImpl(
						new BeforeAfterEnhancer() {

							@Override
							public void before() {
								assertFalse(beforeSeen);
								beforeSeen = true;
							}

							@Override
							public Object after(Object instance) {
								assertTrue(beforeSeen);
								assertNull(afterSeen);
								afterSeen = instance;
								return "Hello";
							}
						}).compile(ctx, new SupplierRecipe() {

					@Override
					protected Class<?> compileImpl(GeneratorAdapter mv,
							MethodCompilationContext ctx) {
						mv.push(1);
						return int.class;
					}
				});
			}
		}).get();

		assertEquals("Hello", result);
		assertTrue(beforeSeen);
		assertEquals(1, afterSeen);
	}
}
