package com.github.ruediste.salta.core;

/**
 * Factory to create {@link CreationRecipe}s. Provided by {@link Binding}s
 */
public interface CreationRecipeFactory {
	/**
	 * Create a recipe for this binding. The result will typically be cached.
	 * Any expensive operations to create the recipe should be done in this
	 * method
	 */
	public CreationRecipe<?> createRecipe();

	public default TransitiveCreationRecipe createTransitiveDirect(
			ContextualInjector ctx) {
		return null;
	}
}
