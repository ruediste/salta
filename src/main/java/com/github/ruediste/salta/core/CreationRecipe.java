package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Describes how to create an instance for a {@link Binding}
 */
public abstract class CreationRecipe extends AttachedPropertyBearerBase {

	public abstract Object createInstance(ContextualInjector injector);

}
