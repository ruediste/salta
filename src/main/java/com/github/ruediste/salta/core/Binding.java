package com.github.ruediste.salta.core;

public abstract class Binding {

	private volatile CreationRecipe recipe;

	public final synchronized CreationRecipe getCreationRecipe() {
		if (recipe == null)
			recipe = createRecipe();
		return recipe;
	}

	/**
	 * Create a recipe for this binding. This method will only be called once
	 * per binding instance.
	 */
	protected abstract CreationRecipe createRecipe();
}
