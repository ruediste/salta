package com.github.ruediste.salta.standard.config;

import java.util.function.Function;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

final class DefaultScope implements Scope {

	@Override
	public String toString() {
		return "Default";
	}

	@Override
	public Function<RecipeCreationContext, SupplierRecipe> createRecipe(
			Binding binding, TypeToken<?> requestedType) {
		return binding.getOrCreateRecipe();
	}
}