package com.github.ruediste.salta.core.compile;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.Injector;

public class SupplierRecipeImplTest {

	private Injector injector;

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				binder().getConfiguration().config.config.creationRules
						.add(new CreationRule() {

							@Override
							public Function<RecipeCreationContext, SupplierRecipe> apply(
									CoreDependencyKey<?> key) {
								if (key.getRawType().equals(int.class)) {
									return ctx -> new SupplierRecipeImpl(
											() -> 2);
								}
								return null;
							}

						});
			}
		}, new JSR330Module());

	}

	private static class A {
		@Inject
		int i;
	}

	@Test
	public void primitive() {
		assertEquals(2, (int) injector.getInstance(int.class));
		assertEquals(2, injector.getInstance(A.class).i);
	}
}
