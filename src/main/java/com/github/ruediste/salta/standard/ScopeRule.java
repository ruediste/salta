package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Rule to determine the Scope for a type
 */
public interface ScopeRule {

	void configureScope(TypeToken<?> type, StandardCreationRecipe recipe);
}
