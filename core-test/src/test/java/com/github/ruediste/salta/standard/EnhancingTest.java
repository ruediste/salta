package com.github.ruediste.salta.standard;

import java.util.Optional;

import org.junit.Test;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.config.EnhancementRule;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.google.common.reflect.TypeToken;

public class EnhancingTest {

	private static class TestA {
		int theFoo() {
			return 1;
		}
	}

	private static class Enhancer {

	}

	@Test
	public void doesEnhance() {
		Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				getConfiguration().enhancerFactories.add(new EnhancementRule() {

					@Override
					public Optional<RecipeEnhancer> getEnhancer(
							RecipeCreationContext ctx, TypeToken<?> type) {
						if (type.getRawType().equals(TestA.class)) {
							return Optional.of(new RecipeEnhancer() {

								@Override
								public Class<?> compile(
										MethodCompilationContext compilationContext,
										SupplierRecipe innerRecipe) {
									// TODO Auto-generated method stub
									return null;
								}
							});
						}
						return Optional.empty();
					}
				});
			}
		}, new JSR330Module());
	}
}
