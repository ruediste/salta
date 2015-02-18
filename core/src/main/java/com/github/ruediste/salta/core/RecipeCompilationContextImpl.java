package com.github.ruediste.salta.core;

import org.objectweb.asm.commons.GeneratorAdapter;

public class RecipeCompilationContextImpl extends RecipeCompilationContextBase {
	GeneratorAdapter mv;

	public RecipeCompilationContextImpl(CreationRecipeCompiler compiler) {
		super(compiler);
	}

	@Override
	public GeneratorAdapter getMv() {
		return mv;
	}

}
