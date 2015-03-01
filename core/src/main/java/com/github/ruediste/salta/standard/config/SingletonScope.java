package com.github.ruediste.salta.standard.config;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
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

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext compilationContext) {
				compilationContext.addFieldAndLoad((Class) type.getRawType(),
						instance);
				return type.getRawType();
			}
		};
	}
}