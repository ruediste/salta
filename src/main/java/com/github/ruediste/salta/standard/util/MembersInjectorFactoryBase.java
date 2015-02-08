package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.reflect.TypeToken;

public abstract class MembersInjectorFactoryBase implements
		RecipeMembersInjectorFactory {

	@Override
	public <T> List<RecipeMembersInjector<T>> createInjectors(
			TypeToken<T> typeToken) {
		ArrayList<RecipeMembersInjector<T>> result = new ArrayList<>();

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
					result.add(new FixedFieldRecipeMembersInjector<T>(f,
							dependency));
				}
			}

			for (Method method : t.getRawType().getDeclaredMethods()) {

				if (isInjectableMethod(t, method, overrideIndex)) {
					method.setAccessible(true);

					// create dependencies
					ArrayList<CoreDependencyKey<?>> args = new ArrayList<>();
					Parameter[] parameters = method.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						Parameter parameter = parameters[i];
						@SuppressWarnings({ "unchecked", "rawtypes" })
						CoreDependencyKey<Object> dependency = new InjectionPoint<>(
								(TypeToken) t.resolveType(parameter
										.getParameterizedType()), method,
								parameter, i);
						args.add(dependency);
					}

					// add injector
					result.add(new FixedMethodRecipeMembersInjector<T>(method,
							args));
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
