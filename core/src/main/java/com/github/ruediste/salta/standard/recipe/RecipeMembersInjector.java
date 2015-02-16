package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;

public interface RecipeMembersInjector {
	void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext);
}
