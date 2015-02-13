package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CreationRecipe;

/**
 * Factory to create {@link CreationRecipe}s. Provided by {@link Binding}s
 */
public interface CreationRecipeFactory {
	/**
	 * Create a recipe for this binding. The result will typically be cached.
	 * Any expensive operations to create the recipe should be done in this
	 * method
	 * @param ctx TODO
	 */
	public CreationRecipe createRecipe(BindingContext ctx);

}
