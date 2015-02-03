package com.github.ruediste.simpledi.core;

import com.google.common.reflect.TypeToken;

public interface ContextualInjector {

	public <T> T createInstance(Dependency<T> key);

	/**
	 * Create an instantiator for the given type. Uses
	 * {@link InjectorConfiguration#instantiatorRules}
	 */
	public <T> Instantiator<T> createInstantiator(TypeToken<T> type);
}
