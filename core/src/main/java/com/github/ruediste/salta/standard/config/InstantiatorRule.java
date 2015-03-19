package com.github.ruediste.salta.standard.config;

import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInstantiator} for a given type
 */
public interface InstantiatorRule {

	/**
	 * Return a function creating a {@link RecipeInstantiator}, or
	 * {@link Optional#empty()} if the next rule should be tried
	 */
	Optional<Function<RecipeCreationContext, RecipeInstantiator>> apply(
			TypeToken<?> type);
}
