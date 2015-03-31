package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class FixedMethodInvocationFunctionRecipeTest {

	@Test
	public void checkAssumption() throws Exception {
		assertTrue(void.class.equals(getClass().getMethod("checkAssumption")
				.getReturnType()));
	}

	int count;

	void a() {
		count++;
	}

	@Test
	public void invokeDynamic() throws Exception {
		RecipeCompiler compiler = new RecipeCompiler();
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
}
