package com.github.ruediste.salta.core;

import java.util.function.Consumer;

public interface RecipeCreationContext {

	CreationRecipe getRecipe(CoreDependencyKey<?> dependency);

	CreationRecipe getRecipeInNewContext(CoreDependencyKey<?> dependency);

	CreationRecipe getOrCreateRecipe(Binding binding);

	CompiledCreationRecipe compileRecipe(CreationRecipe recipe);

	void queueAction(Consumer<RecipeCreationContext> action);
}