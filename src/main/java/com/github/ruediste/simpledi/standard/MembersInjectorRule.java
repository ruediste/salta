package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.standard.recipe.RecipeInstantiator;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInstantiator} for a given type
 */
public interface MembersInjectorRule {

	void addMembersInjectors(TypeToken<?> type, StandardCreationRecipe recipe);

}
