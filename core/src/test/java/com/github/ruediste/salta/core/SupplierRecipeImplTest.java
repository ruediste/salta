package com.github.ruediste.salta.core;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;

public class SupplierRecipeImplTest {

	private Injector injector;

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				binder().getConfiguration().config.creationRules
						.add(new DependencyFactoryRule() {

							@Override
							public SupplierRecipe apply(
									CoreDependencyKey<?> key,
									RecipeCreationContext ctx) {
								if (key.getRawType().equals(int.class)) {
									return new SupplierRecipeImpl(() -> 2);
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
