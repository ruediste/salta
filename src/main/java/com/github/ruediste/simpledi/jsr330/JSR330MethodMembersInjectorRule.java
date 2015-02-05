package com.github.ruediste.simpledi.jsr330;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.simpledi.standard.util.MethodMembersInjectorRuleBase;
import com.google.common.reflect.TypeToken;

public class JSR330MethodMembersInjectorRule extends
		MethodMembersInjectorRuleBase {

	@Override
	protected boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method) {
		if (Modifier.isAbstract(method.getModifiers()))
			return false;
		return method.isAnnotationPresent(Inject.class);
	}

}
