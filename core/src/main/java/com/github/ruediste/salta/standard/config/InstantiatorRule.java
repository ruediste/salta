package com.github.ruediste.salta.standard.config;

import java.util.Optional;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInstantiator} for a given type
 */
public interface InstantiatorRule {

	/**
	 * Create the instantiator or return {@link Optional#empty()} if the next
	 * rule should be tried.
	 */
	Optional<RecipeInstantiator> apply(RecipeCreationContext ctx,
			TypeToken<?> type);
}
