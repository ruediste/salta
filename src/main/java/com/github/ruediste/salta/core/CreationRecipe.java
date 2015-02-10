package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public abstract class CreationRecipe<T> extends AttachedPropertyBearerBase {

	public Scope scope;

	public CreationRecipe<T> withScope(Scope scope) {
		this.scope = scope;
		return this;
	}

	public abstract T createInstance(ContextualInjector injector);

	public abstract void injectMembers(T instance, ContextualInjector injector);

}
