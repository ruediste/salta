package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.core.ContextualInjector;



/**
 * Instantiate an instance
 */
public interface Instantiator<T> {
	T instantiate(ContextualInjector injector);
}
