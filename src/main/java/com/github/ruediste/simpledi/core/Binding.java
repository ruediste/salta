package com.github.ruediste.simpledi.core;

public interface Binding {

	/**
	 * Create a recipe for this binding. If any configuration information is
	 * required to crate the binding, the binding must be created when this
	 * method is called to make sure up-to-date information is used.
	 */
	CreationRecipe createRecipe();
}
