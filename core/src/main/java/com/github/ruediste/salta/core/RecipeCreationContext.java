package com.github.ruediste.salta.core;

import java.util.Optional;

import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public interface RecipeCreationContext {

	SupplierRecipe getRecipe(CoreDependencyKey<?> dependency);

	Optional<SupplierRecipe> tryGetRecipe(CoreDependencyKey<?> dependency);

	SupplierRecipe getRecipeInNewContext(CoreDependencyKey<?> dependency);

	SupplierRecipe getOrCreateRecipe(Binding binding);

	RecipeCompiler getCompiler();

	void queueAction(Runnable action);

	/**
	 * get {@link CoreInjector#recipeLock}
	 */
	Object getRecipeLock();
}