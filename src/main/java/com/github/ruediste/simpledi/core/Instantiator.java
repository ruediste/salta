package com.github.ruediste.simpledi.core;



/**
 * Instantiate an instance
 */
public interface Instantiator<T> {
	T instantiate(ContextualInjector injector);
}
