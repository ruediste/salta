package com.github.ruediste.salta.standard.recipe;

import java.util.List;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.google.common.reflect.TypeToken;

/**
 * Factory producing a list of {@link RecipeMembersInjector}s given a type.
 * Usually, multiple factories are used on a single type and all produced
 * injectors are used
 */
public interface RecipeMembersInjectorFactory {

	/**
	 * Create {@link RecipeMembersInjector}s for the given type
	 */
	List<RecipeMembersInjector> createMembersInjectors(
			RecipeCreationContext ctx, TypeToken<?> type);
}
