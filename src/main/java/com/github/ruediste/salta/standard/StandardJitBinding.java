package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.JITBinding;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends JITBinding {

	public CreationRecipeFactory recipeFactory;

	@Override
	public CreationRecipe createRecipe(BindingContext ctx) {
		return recipeFactory.createRecipe(ctx);
	}

}
