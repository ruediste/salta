package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.config.RecipeInitializerFactory;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeInitializer;
import com.github.ruediste.salta.standard.recipe.RecipeInitializer;
import com.google.common.reflect.TypeToken;

public abstract class RecipeInitializerFactoryBase implements
		RecipeInitializerFactory {

	private CoreInjectorConfiguration config;

	public RecipeInitializerFactoryBase(CoreInjectorConfiguration config) {
		this.config = config;

	}

	@Override
	public List<RecipeInitializer> getInitializers(RecipeCreationContext ctx,
			TypeToken<?> typeToken) {
		ArrayList<RecipeInitializer> result = new ArrayList<>();

		MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
		// iterate over super types, always processing supertypes before
		// subtypes
		for (TypeToken<?> t : overrideIndex.getAncestors()) {

			for (Method method : t.getRawType().getDeclaredMethods()) {
				if (Modifier.isStatic(method.getModifiers())
						|| Modifier.isAbstract(method.getModifiers()))
					continue;
				if (isInitializer(t, method, overrideIndex)) {
					method.setAccessible(true);

					// create dependencies
					ArrayList<SupplierRecipe> args = new ArrayList<>();
					Parameter[] parameters = method.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						Parameter parameter = parameters[i];
						@SuppressWarnings({ "unchecked", "rawtypes" })
						CoreDependencyKey<Object> dependency = new InjectionPoint<>(
								(TypeToken) t.resolveType(parameter
										.getParameterizedType()), method,
								parameter, i);
						args.add(ctx.getRecipe(dependency));
					}

					// add injector
					result.add(new FixedMethodRecipeInitializer(method, args));
				}
			}
		}
		return result;
	}

	protected abstract boolean isInitializer(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex overrideIndex);

}
