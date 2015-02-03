package com.github.ruediste.simpledi.core;


/**
 * Listens for injections into instances of type {@code I}. Useful for
 * performing further injections, post-injection initialization, and more.
 */
public interface InjectionListener<T> {
	/**
	 * Invoked after the {@link MembersInjector}s. Can return the same instance
	 * or another instance.
	 * @param injector TODO
	 */
	T afterInjection(T injectee, ContextualInjector injector);
}
