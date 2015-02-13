package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.standard.MembersInjector;

/**
 * Listens for injections into instances. Useful for performing post-injection
 * initialization, wrapping in proxies, and more.
 */
public interface RecipeInjectionListener {
	/**
	 * Invoked after the {@link MembersInjector}s. Can return the same instance
	 * or another instance.
	 */
	Object afterInjection(Object injectee);

	void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
