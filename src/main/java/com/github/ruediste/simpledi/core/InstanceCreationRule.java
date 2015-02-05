package com.github.ruediste.simpledi.core;

import java.util.function.Supplier;

/**
 * Rule to create an instance for a {@link Dependency}. Used by the
 * {@link Injector} before checking the {@link StaticBinding}s
 */
public interface InstanceCreationRule {
	<T> Supplier<T> apply(Dependency<T> dependency);
}