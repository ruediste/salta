package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Base class for bindings
 */
public abstract class Binding extends AttachedPropertyBearerBase {

	/**
	 * Create a recipe for this binding. The result will typically be cached.
	 * Any expensive operations to create the recipe should be done in this
	 * method
	 */
	public abstract CreationRecipe createRecipe(BindingContext ctx);

}
