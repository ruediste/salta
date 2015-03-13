package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.reflect.TypeToken;

public abstract class MembersInjectorFactoryBase implements
		RecipeMembersInjectorFactory {

	private StandardInjectorConfiguration config;

	public MembersInjectorFactoryBase(StandardInjectorConfiguration config) {
		this.config = config;

	}

	@Override
	public List<RecipeMembersInjector> createInjectors(
			RecipeCreationContext ctx, TypeToken<?> typeToken) {
		ArrayList<RecipeMembersInjector> result = new ArrayList<>();

		MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
		// iterate over super types, always processing supertypes before
		// subtypes
		for (TypeToken<?> t : overrideIndex.getAncestors()) {
			for (Field f : t.getRawType().getDeclaredFields()) {
				if (isInjectableField(t, f)) {
					f.setAccessible(true);

					// create dependency
					CoreDependencyKey<?> dependency = new InjectionPoint<>(
							t.resolveType(f.getGenericType()), f, f, null);

					// add injector
					result.add(new FixedFieldRecipeMembersInjector(f, ctx
							.getRecipe(dependency),
							config.config.injectionStrategy));
				}
			}

			for (Method method : t.getRawType().getDeclaredMethods()) {
				if (Modifier.isStatic(method.getModifiers()))
					continue;
				if (isInjectableMethod(t, method, overrideIndex)) {
					method.setAccessible(true);

					// check that no qualifiers are given on the method itself
					if (config.getRequiredQualifier(method, method) != null) {
						/*
						 * In Scala, fields automatically get accessor methods
						 * with the same name. So we don't do misplaced-binding
						 * annotation detection if the offending method has a
						 * matching field.
						 */
						boolean fieldFound = false;
						for (Field f : method.getDeclaringClass()
								.getDeclaredFields()) {
							if (f.getName().equals(method.getName())) {
								fieldFound = true;
								break;
							}
						}
						if (!fieldFound)
							throw new SaltaException(
									"Qualifier has been specified on "
											+ method
											+ ".\nSpecify qualifiers on parameters instead");
					}

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
					result.add(new FixedMethodRecipeMembersInjector(method,
							args, config.config.injectionStrategy));
				}
			}
		}
		return result;
	}

	protected abstract boolean isInjectableField(TypeToken<?> declaringType,
			Field f);

	protected abstract boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex overrideIndex);

}
