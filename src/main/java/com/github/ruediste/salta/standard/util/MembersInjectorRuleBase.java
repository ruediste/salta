package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.standard.MembersInjectorRule;
import com.github.ruediste.salta.standard.StandardInjectionPoint;
import com.github.ruediste.salta.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

public abstract class MembersInjectorRuleBase implements
		MembersInjectorRule {

	@Override
	public void addMembersInjectors(TypeToken<?> typeToken,
			StandardCreationRecipe recipe) {

		MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
		// iterate over super types, always processing supertypes before
		// subtypes
		for (TypeToken<?> t : overrideIndex.getAncestors()) {
			for (Field f : t.getRawType().getDeclaredFields()) {
				if (isInjectableField(t, f)) {
					f.setAccessible(true);

					// create dependency
					Dependency<?> dependency = new Dependency<>(t.resolveType(f
							.getGenericType()), new StandardInjectionPoint(f,
							f, null));

					// add injector
					recipe.membersInjectors
							.add(new FixedFieldRecipeMembersInjector<Object>(f,
									dependency));
				}
			}

			for (Method method : t.getRawType().getDeclaredMethods()) {

				if (isInjectableMethod(t, method, overrideIndex)) {
					method.setAccessible(true);

					// create dependencies
					ArrayList<Dependency<?>> args = new ArrayList<>();
					Parameter[] parameters = method.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						Parameter parameter = parameters[i];
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Dependency<Object> dependency = new Dependency<Object>(
								(TypeToken) t.resolveType(parameter
										.getParameterizedType()),
								new StandardInjectionPoint(method, parameter, i));
						args.add(dependency);
					}

					// add injector
					recipe.membersInjectors
							.add(new FixedMethodRecipeMembersInjector<Object>(
									method, args));
				}
			}
		}
	}

	protected abstract boolean isInjectableField(TypeToken<?> declaringType,
			Field f);

	protected abstract boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex overrideIndex);
}
