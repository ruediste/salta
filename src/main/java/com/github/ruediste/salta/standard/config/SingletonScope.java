package com.github.ruediste.salta.standard.config;

import org.mockito.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.Scope;
import com.google.common.reflect.TypeToken;

final class SingletonScope implements Scope {

	@Override
	public String toString() {
		return "Singleton";
	}

	@Override
	public CreationRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> type, CreationRecipe innerRecipe) {
		Object instance = ctx.getInstance(innerRecipe);
		return new CreationRecipe() {

			@Override
			public void compile(GeneratorAdapter mv,
					RecipeCompilationContext compilationContext) {
				compilationContext.addAndLoad(
						Type.getDescriptor(type.getRawType()), instance);
			}
		};
	}
}