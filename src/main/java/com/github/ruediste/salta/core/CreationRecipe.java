package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Describes how to create an instance for a {@link Binding}
 */
public abstract class CreationRecipe extends AttachedPropertyBearerBase {

	/**
	 * Compile the recipe. May be called from multiple threads concurrently. Can
	 * be called multiple times per recipe.May not acquire
	 * {@link CoreInjector#recipeLock} or {@link CoreInjector#instantiationLock}
	 * => no calls to the injector methods
	 */
	public abstract void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
