package com.github.ruediste.salta.standard.config;

import java.lang.reflect.Constructor;
import java.util.Optional;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Factory for a {@link RecipeInstantiator} given a constructor
 */
public interface FixedConstructorInstantiationRule {
	/**
	 * Create the {@link RecipeInstantiator} for the given constructor in type
	 * typeToken
	 */
	Optional<RecipeInstantiator> create(TypeToken<?> typeToken,
			RecipeCreationContext ctx, Constructor<?> constructor);
}
