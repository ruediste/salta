package com.github.ruediste.salta.core;


public interface RecipeCreationContext {

	SupplierRecipe getRecipe(CoreDependencyKey<?> dependency);

	SupplierRecipe getRecipeInNewContext(CoreDependencyKey<?> dependency);

	SupplierRecipe getOrCreateRecipe(Binding binding);

	RecipeCompiler getCompiler();

	void queueAction(Runnable action);
}