package com.github.ruediste.salta.standard.config;

import java.util.List;

import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInstantiator} for a given type
 */
public interface MembersInjectorRule {

	/**
	 * Create the {@link RecipeMembersInjector}s. If null is returned, the next
	 * rule is tried
	 */
	List<RecipeMembersInjector> getMembersInjectors(TypeToken<?> type);

}
