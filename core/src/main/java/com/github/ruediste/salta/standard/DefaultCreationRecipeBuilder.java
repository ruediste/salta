package com.github.ruediste.salta.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {
	public Supplier<Function<RecipeCreationContext, SupplierRecipe>> constructionRecipeSupplier;
	public Function<RecipeCreationContext, List<RecipeEnhancer>> enhancersSupplier;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> boundType) {

		constructionRecipeSupplier = () -> {
			Optional<Function<RecipeCreationContext, SupplierRecipe>> tmp = config
					.createConstructionRecipe(boundType);
			if (!tmp.isPresent())
				throw new SaltaException("Cannot find construction rule for "
						+ boundType);
			return tmp.get();
		};

		enhancersSupplier = ctx -> config.createEnhancers(ctx, boundType);
	}

	/**
	 * Build the recipe.
	 */
	public SupplierRecipe build(RecipeCreationContext ctx) {

		// create seed recipe
		SupplierRecipe seedRecipe = constructionRecipeSupplier.get().apply(ctx);

		// apply enhancers
		List<RecipeEnhancer> enhancers = new ArrayList<>(
				enhancersSupplier.apply(ctx));

		SupplierRecipe innerRecipe = applyEnhancers(seedRecipe, enhancers);

		return innerRecipe;
	}

	public static SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			List<RecipeEnhancer> enhancers) {
		Collections.reverse(enhancers);
		SupplierRecipe innerRecipe = createInnerRecipe(enhancers.iterator(),
				seedRecipe);
		return innerRecipe;
	}

	private static SupplierRecipe createInnerRecipe(
			Iterator<RecipeEnhancer> iterator, SupplierRecipe seedRecipe) {
		if (!iterator.hasNext())
			return seedRecipe;

		// consume an enhancer
		RecipeEnhancer enhancer = iterator.next();

		// create a recipe for the rest of the chain
		SupplierRecipe innerRecipe = createInnerRecipe(iterator, seedRecipe);

		// return recipe
		return new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {
				return enhancer.compile(ctx, innerRecipe);
			}

		};
	}

	public static SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			StandardInjectorConfiguration config, RecipeCreationContext ctx,
			TypeToken<?> type) {
		return applyEnhancers(seedRecipe, config.createEnhancers(ctx, type));
	}
}
