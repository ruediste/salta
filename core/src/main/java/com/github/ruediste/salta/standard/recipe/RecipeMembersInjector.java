package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.github.ruediste.salta.standard.config.DefaultConstructionRule;

/**
 * {@link FunctionRecipe} used by the {@link DefaultConstructionRule} to inject
 * members of an instance created with a {@link RecipeInstantiator}.
 */
public interface RecipeMembersInjector extends FunctionRecipe {

}
