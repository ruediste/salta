package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeEnhancer} for a type
 */
public interface EnhancementRule {

	/**
	 * Create a {@link RecipeEnhancer} based on a type. If null is returned, the
	 * result is ignored
	 */
	RecipeEnhancer getEnhancer(RecipeCreationContext ctx, TypeToken<?> type);

}
