package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Method;
import java.util.List;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class FixedMethodRecipeInitializer extends
		FixedMethodInvocationFunctionRecipe implements RecipeInitializer {

	public FixedMethodRecipeInitializer(Method method,
			List<SupplierRecipe> argumentRecipes,
			InjectionStrategy injectionStrategy) {
		super(method, argumentRecipes, injectionStrategy, true);
	}

}
