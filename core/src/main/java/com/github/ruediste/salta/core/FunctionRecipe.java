package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Recipe for compiling a function that accepts one argument and produces a
 * result
 */
public abstract class FunctionRecipe {

	/**
	 * Emit the code to apply the function to an argument. The argument is
	 * passed as top of stack. The result needs to be placed on the top of the
	 * stack as well.
	 * 
	 * @param argumentType
	 *            type of the passed argument
	 * @return type of the result
	 * 
	 */
	public final Class<?> compile(Class<?> argumentType,
			RecipeCompilationContext ctx) {
		return compileImpl(argumentType, ctx.getMv(), ctx);
	}

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack.
	 * 
	 * @param argumentType
	 *            type of the passed argument
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	protected abstract Class<?> compileImpl(Class<?> argumentType,
			GeneratorAdapter mv, RecipeCompilationContext ctx);
}
