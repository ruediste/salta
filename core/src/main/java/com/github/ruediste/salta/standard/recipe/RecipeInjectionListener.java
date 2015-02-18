package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;

/**
 * Listens for injections into instances. Useful for performing post-injection
 * initialization, wrapping in proxies, and more.
 */
public interface RecipeInjectionListener {

	/**
	 * Compile this recipe. The inner recipe will create the injected instance
	 * as the top of the stack
	 */
	void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext,
			CreationRecipe innerRecipe);
}
