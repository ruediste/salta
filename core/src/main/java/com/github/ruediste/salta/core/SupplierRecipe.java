package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Recipe for compiling a supplier of a value
 */
public abstract class SupplierRecipe {

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack. Generally, there is an implicit expected type
	 * for the TOS value. However, the produced value does not need to have the
	 * expected type, since this is often not possible due to types which are
	 * not visible from the compiled recipe. The caller of compile has to allow
	 * for all typecasts which are performed by
	 * {@link RecipeCompilationContext#cast(Class, Class)}.
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
