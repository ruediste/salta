package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.MembersInjector;

/**
 * Listens for injections into instances of type {@code T}. Useful for
 * performing post-injection initialization, wrapping in proxies, and more.
 */
public interface RecipeInjectionListener<T> {
	/**
	 * Invoked after the {@link MembersInjector}s. Can return the same instance
	 * or another instance.
	 */
	T afterInjection(T injectee, ContextualInjector injector);
}
