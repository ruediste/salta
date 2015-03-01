package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends JITBinding {

	public CreationRecipeFactory recipeFactory;
	private TypeToken<?> type;

	public StandardJitBinding(TypeToken<?> type) {
		this.type = type;
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
		return recipeFactory.createRecipe(ctx);
	}

	@Override
	public String toString() {
		return "StandardJitBinding(" + type + ")";
	}
}
