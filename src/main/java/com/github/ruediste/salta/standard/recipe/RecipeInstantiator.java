package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;

/**
 * Instantiate an instance for a {@link DefaultCreationRecipeBuilder}. The
 * instantiator should contain a very detailed description of what to do. All
 * reflection and decision making should happen upon instantiation
 */
public interface RecipeInstantiator<T> {
	T instantiate(ContextualInjector injector);

	TransitiveRecipeInstantiator createTransitive(BindingContext ctx);
}
