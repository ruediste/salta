package com.github.ruediste.salta;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.standard.Module;

/**
 * A rule defining how to modify an {@link CreationRecipe} based on an
 * {@link InstantiationRequest}. The rules are defined in {@link Module}s
 */
@Deprecated
public interface Rule {
	/**
	 * Modify a recipe based on key an injection point
	 * 
	 * @param recipe
	 *            recipe to construct
	 * @param key
	 *            key beeing requested
	 */
	void apply(CreationRecipe recipe, Dependency<?> key);
}
