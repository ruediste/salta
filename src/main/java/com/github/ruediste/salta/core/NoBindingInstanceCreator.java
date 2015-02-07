package com.github.ruediste.salta.core;

/**
 * Creator of instances without a binding. Instantiated through
 * {@link NoBindingInstanceCreationRule}, which are evaluated for a {@link Dependency}
 * before checking the {@link StaticBinding}s and the {@link JITBinding}s.
 */
public interface NoBindingInstanceCreator<T> {

	T createInstance(ContextualInjector injector);
}
