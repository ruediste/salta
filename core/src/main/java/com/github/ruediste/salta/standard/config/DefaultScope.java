package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.Scope;
import com.google.common.reflect.TypeToken;

final class DefaultScope implements Scope {

	@Override
	public String toString() {
		return "Default";
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> type, SupplierRecipe innerRecipe) {
		return innerRecipe;
	}
}