package com.github.ruediste.simpledi.standard.recipe;

import com.github.ruediste.simpledi.core.ContextualInjector;

/**
 * Instantiate an instance for a {@link StandardCreationRecipe}. The
 * instantiator should contain a very detailed description of what to do. All
 * reflection and decision making should happen upon instantiation
 */
public interface RecipeInstantiator<T> {
	T instantiate(ContextualInjector injector);
}
