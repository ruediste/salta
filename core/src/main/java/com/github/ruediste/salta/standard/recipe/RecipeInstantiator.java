package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;

/**
 * Instantiate an instance for a {@link DefaultCreationRecipeBuilder}. The
 * instantiator should contain a very detailed description of what to do. All
 * reflection and decision making should happen upon instantiation
 */
public interface RecipeInstantiator {

	void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
