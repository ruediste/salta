package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Rule to determine the Scope for a type
 */
public interface ScopeRule {

	void configureScope(TypeToken<?> type, StandardCreationRecipe recipe);
}
