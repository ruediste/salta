package com.github.ruediste.simpledi;


/**
 * A rule defining how to modify an {@link CreationRecipe} based on an
 * {@link InstantiationRequest}. The rules are defined in {@link Module}s
 */
public interface Rule {
	/**
	 * Modify a recipe based on key an injection point
	 * 
	 * @param recipe
	 *            recipe to construct
	 * @param key
	 *            key beeing requested
	 */
	void apply(CreationRecipe recipe, InstanceRequest<?> key);
}
