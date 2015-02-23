package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.SupplierRecipe;

/**
 * Listens for injections into instances. Useful for performing post-injection
 * initialization, wrapping in proxies, and more.
 */
public interface RecipeInjectionListener {

	/**
	 * Compile this recipe. The inner recipe will create the injected instance
	 * as the top of the stack
	 */
	Class<?> compile(RecipeCompilationContext compilationContext,
			SupplierRecipe innerRecipe);
}
