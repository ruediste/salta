package com.github.ruediste.salta.core;

/**
 * Recipe to create an instance including all it's dependencies.
 */
public interface TransitiveCreationRecipe {

	Object createInstance(ContextualInjector injector);
}
