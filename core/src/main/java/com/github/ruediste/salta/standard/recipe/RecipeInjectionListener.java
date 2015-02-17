package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;

/**
 * Listens for injections into instances. Useful for performing post-injection
 * initialization, wrapping in proxies, and more.
 */
public interface RecipeInjectionListener {

	/**
	 * Compile this recipe. The top of the stack contains the injected instance.
	 * Can be replaced if desired.
	 */
	void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
