package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Recipe for compiling a supplier of a value
 */
public abstract class SupplierRecipe {

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack.
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	public final Class<?> compile(RecipeCompilationContext ctx) {
		return compileImpl(ctx.getMv(), ctx);
	}

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack.
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	protected abstract Class<?> compileImpl(GeneratorAdapter mv,
			RecipeCompilationContext ctx);
}
