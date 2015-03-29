package com.github.ruediste.salta.standard;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {

	/**
	 * Build the recipe.
	 */
	public static SupplierRecipe build(StandardInjectorConfiguration config,
			TypeToken<?> boundType, RecipeCreationContext ctx) {

		// create seed recipe
		Optional<Function<RecipeCreationContext, SupplierRecipe>> seedRecipe = config.construction
				.createConstructionRecipe(boundType);
		if (!seedRecipe.isPresent())
			throw new SaltaException("Cannot find construction recipe for "
					+ boundType);

		// apply enhancers
		SupplierRecipe innerRecipe = applyEnhancers(
				seedRecipe.get().apply(ctx),
				config.construction.createEnhancers(ctx, boundType));

		return innerRecipe;
	}

	public static SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			List<RecipeEnhancer> enhancers) {

		SupplierRecipe result = seedRecipe;
		for (RecipeEnhancer enhancer : enhancers) {
			SupplierRecipe innerRecipe = result;
			result = new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext ctx) {
					return enhancer.compile(ctx, innerRecipe);
				}

			};
		}
		return result;
	}

	public static SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			StandardInjectorConfiguration config, RecipeCreationContext ctx,
			TypeToken<?> type) {
		return config.construction.applyEnhancers(seedRecipe, ctx, type);
	}
}
