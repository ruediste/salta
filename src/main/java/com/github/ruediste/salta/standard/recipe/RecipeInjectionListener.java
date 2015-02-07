package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.MembersInjector;

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
