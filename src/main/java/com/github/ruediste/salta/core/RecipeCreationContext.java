package com.github.ruediste.salta.core;

public interface RecipeCreationContext {

	CreationRecipe getRecipe(CoreDependencyKey<?> dependency);

	<T> CreationRecipe getOrCreateRecipe(Binding binding,
			RecipeCreationContext ctx);

	Object getInstance(CreationRecipe innerRecipe);
}