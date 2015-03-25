package com.github.ruediste.salta.core.compile;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;

public class SupplierRecipeImplTest {

	private Injector injector;

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				binder().getConfiguration().config.creationPipeline.creationRules
						.add(new CreationRule() {

							@Override
							public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
									CoreDependencyKey<?> key) {
								if (key.getRawType().equals(int.class)) {
									return Optional
											.of(ctx -> new SupplierRecipeImpl(
													() -> 2));
								}
								return Optional.empty();
							}

						});
			}
		});

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
