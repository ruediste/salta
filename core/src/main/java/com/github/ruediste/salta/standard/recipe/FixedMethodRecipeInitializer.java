package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class FixedMethodRecipeInitializer extends
		FixedMethodInvocationFunctionRecipe implements RecipeInitializer {

	public FixedMethodRecipeInitializer(Method method,
			List<SupplierRecipe> argumentRecipes) {
		super(method, argumentRecipes);
	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext compilationContext) {
		mv.dup();
		Class<?> returnType = super
				.compileImpl(argType, mv, compilationContext);
		compilationContext.pop(returnType);
		return argType;
	}
}
