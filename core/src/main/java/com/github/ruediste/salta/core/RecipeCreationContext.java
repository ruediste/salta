package com.github.ruediste.salta.core;

import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;


public interface RecipeCreationContext {

	SupplierRecipe getRecipe(CoreDependencyKey<?> dependency);

	SupplierRecipe getRecipeInNewContext(CoreDependencyKey<?> dependency);

	SupplierRecipe getOrCreateRecipe(Binding binding);

	RecipeCompiler getCompiler();

	void queueAction(Runnable action);
}