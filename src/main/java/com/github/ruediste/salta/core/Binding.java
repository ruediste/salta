package com.github.ruediste.salta.core;

/**
 * Base class for bindings
 */
public abstract class Binding {

	public abstract CreationRecipeFactory getRecipeFactory();

	/**
	 * Create a recipe for this binding. The result will typically be cached.
	 * Any expensive operations to create the recipe should be done in this
	 * method
	 */
	public abstract CreationRecipe<?> createRecipe();
}
