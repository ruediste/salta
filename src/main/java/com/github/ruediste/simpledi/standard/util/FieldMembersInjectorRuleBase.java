package com.github.ruediste.simpledi.standard.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.standard.MembersInjectorRule;
import com.github.ruediste.simpledi.standard.StandardInjectionPoint;
import com.github.ruediste.simpledi.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

public abstract class FieldMembersInjectorRuleBase implements MembersInjectorRule {

	@Override
	public void addMembersInjectors(TypeToken<?> type,
			StandardCreationRecipe recipe) {

		// iterate over super types, always processing supertypes before
		// subtypes
		for (TypeToken<?> t : Lists.reverse(new ArrayList<>(type.getTypes()))) {
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
		}
	}

	protected abstract boolean isInjectableField(TypeToken<?> declaringType, Field f);
}
