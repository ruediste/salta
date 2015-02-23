package com.github.ruediste.salta.standard.config;

import org.mockito.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.Scope;
import com.google.common.reflect.TypeToken;

public class SingletonScope implements Scope {

	@Override
	public String toString() {
		return "Singleton";
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> type, SupplierRecipe innerRecipe) {
		Object instance = ctx.getCompiler().compileSupplier(innerRecipe)
				.getNoThrow();
		return new SupplierRecipe() {

			@Override
			public Class<?> compileImpl(GeneratorAdapter mv,
					RecipeCompilationContext compilationContext) {
				compilationContext.addFieldAndLoad(
						Type.getDescriptor(type.getRawType()), instance);
				return type.getRawType();
			}
		};
	}
}