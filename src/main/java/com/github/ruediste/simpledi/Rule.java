package com.github.ruediste.simpledi;

import java.util.function.Supplier;

/**
 * A rule defining how to modify an {@link InstantiationRecipe} based on an
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
	 * @param injectionPoint
	 *            if accessed, the receipe is marked as injection point specific
	 */
	void apply(InstantiationRecipe recipe, Key<?> key,
			Supplier<InjectionPoint> injectionPoint);
}
