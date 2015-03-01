package com.github.ruediste.salta.core.compile;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A recipe for compiling something a complete method body.
 */
public abstract class MethodRecipe {
	/**
	 * Emit the code for the method. The method already has
	 * {@link MethodVisitor#visitCode()} called, and
	 * {@link MethodVisitor#visitMaxs(int, int)} and
	 * {@link MethodVisitor#visitEnd()} will be called afterwards
	 */
	public final void compile(MethodCompilationContext ctx) {
		compileImpl(ctx.getMv(), ctx);
	}

	/**
	 * Emit the code for the method
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	protected abstract void compileImpl(GeneratorAdapter mv,
			MethodCompilationContext ctx);
}
