package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInstantiator} for a given type
 */
public interface InstantiatorRule {

	/**
	 * Create the instantiator or return null if the next rule should be tried.
	 */
	<T> RecipeInstantiator<T> apply(TypeToken<T> type);
}
