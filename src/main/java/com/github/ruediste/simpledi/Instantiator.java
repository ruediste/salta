package com.github.ruediste.simpledi;

/**
 * Instantiate an instance
 */
public interface Instantiator<T> {
	T instantiate(Dependency<T> key, ContextualInjector injector);
}
