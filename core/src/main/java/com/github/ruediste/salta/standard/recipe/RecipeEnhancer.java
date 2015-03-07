package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * Enhances an instance. Typically wrapps the instance in a proxy.
 */
public interface RecipeEnhancer {

	/**
	 * Compile this recipe. The inner recipe will create the injected instance
	 * as the top of the stack
	 */
	Class<?> compile(MethodCompilationContext compilationContext,
			SupplierRecipe innerRecipe);
}
