package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * Factory to create {@link SupplierRecipe}s. Used by
 * {@link StandardStaticBinding}s and {@link StandardJitBinding}s
 */
public interface CreationRecipeFactory {
	/**
	 * Create a recipe for this binding. The result will typically be cached.
	 * Any expensive operations to create the recipe should be done in this
	 * method
	 */
	public SupplierRecipe createRecipe(RecipeCreationContext ctx);

}
