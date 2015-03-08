package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class FixedMethodRecipeMembersInjector extends
		FixedMethodInvocationFunctionRecipe implements RecipeMembersInjector {

	public FixedMethodRecipeMembersInjector(Method method,
			List<SupplierRecipe> argumentRecipes,
			InjectionStrategy injectionStrategy) {
		super(method, argumentRecipes, injectionStrategy);
	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {
		mv.dup();
		Class<?> returnType = super.compileImpl(argType, mv, ctx);
		ctx.pop(returnType);
		return argType;
	}
}
