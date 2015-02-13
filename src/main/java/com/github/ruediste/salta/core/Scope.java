package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A Scope defines a visibility for an instance. The scope can either reuse an
 * instance or decide to create a new instance.
 */
public interface Scope {

	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext,
			Runnable instantiationCompilation);
}
