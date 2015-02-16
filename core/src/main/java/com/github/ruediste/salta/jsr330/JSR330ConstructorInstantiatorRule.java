package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.google.common.reflect.TypeToken;

public class JSR330ConstructorInstantiatorRule extends
		ConstructorInstantiatorRuleBase {

	@Override
	protected Integer getConstructorPriority(Constructor<?> c) {
		if (c.isAnnotationPresent(Inject.class))
			return 2;
		boolean isPrivateInnerClass = c.getDeclaringClass().getEnclosingClass() != null
				&& Modifier.isPrivate(c.getDeclaringClass().getModifiers());

		if (c.getParameterCount() == 0
				&& (Modifier.isPublic(c.getModifiers()) || isPrivateInnerClass))
			return 1;
		return null;
	}

	@Override
	protected ProvisionException noConstructorFound(TypeToken<?> typeToken,
			Class<?> clazz) {
		Class<?> enclosingClass = clazz.getEnclosingClass();
		if (enclosingClass != null) {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (c.getParameterCount() == 1
						&& enclosingClass.equals(c.getParameterTypes()[0])) {
					return new ProvisionException(
							"No suitable constructor found for inner non-static class "
									+ typeToken
									+ ".\nCannot instantiate non-static inner classes. Forgotten to make class static?");
				}
			}
		}
		return super.noConstructorFound(typeToken, clazz);
	}
}
