package com.github.ruediste.salta.core;

/**
 * Creator of instances without a binding. Instantiated through
 * {@link DependencyFactoryRule}, which are evaluated for a
 * {@link CoreDependencyKey} before checking the {@link StaticBinding}s and the
 * {@link JITBinding}s.
 */
public interface DependencyFactory<T> {

	T createInstance(ContextualInjector injector);
}
