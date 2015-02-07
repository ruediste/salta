package com.github.ruediste.salta.core;

/**
 * Rule to create an instance for a {@link Dependency}. Used by the
 * {@link Injector} before checking the {@link StaticBinding}s
 */
public interface NoBindingInstanceCreationRule {
	<T> NoBindingInstanceCreator<T> apply(Dependency<T> dependency);
}
