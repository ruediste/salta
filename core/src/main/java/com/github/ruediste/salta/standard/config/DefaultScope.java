package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

final class DefaultScope implements Scope {

	@Override
	public String toString() {
		return "Default";
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, CoreDependencyKey<?> requestedKey) {
		return binding.getOrCreateRecipe(ctx);
	}
}