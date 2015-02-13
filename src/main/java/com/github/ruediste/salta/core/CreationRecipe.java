package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Describes how to create an instance for a {@link Binding}
 */
public abstract class CreationRecipe extends AttachedPropertyBearerBase {

	public abstract void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
