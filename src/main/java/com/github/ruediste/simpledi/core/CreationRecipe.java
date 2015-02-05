package com.github.ruediste.simpledi.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public abstract class CreationRecipe extends AttachedPropertyBearerBase {

	public Scope scope;

	public abstract Object createInstance(ContextualInjector injector);
}
