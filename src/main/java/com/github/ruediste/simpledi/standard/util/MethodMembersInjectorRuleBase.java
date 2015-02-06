package com.github.ruediste.simpledi.standard.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.standard.MembersInjectorRule;
import com.github.ruediste.simpledi.standard.StandardInjectionPoint;
import com.github.ruediste.simpledi.standard.recipe.FixedMethodRecipeMembersInjector;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

public abstract class MethodMembersInjectorRuleBase implements
		MembersInjectorRule {

	@Override
	public void addMembersInjectors(TypeToken<?> typeToken,
			StandardCreationRecipe recipe) {

		MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
		// iterate over super types, always processing supertypes before
		// subtypes
		for (TypeToken<?> t : overrideIndex.getAncestors()) {
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

	protected abstract boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex overrideIndex);
}
