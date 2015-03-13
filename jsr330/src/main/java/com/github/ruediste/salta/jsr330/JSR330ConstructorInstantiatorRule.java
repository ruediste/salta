package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;

public class JSR330ConstructorInstantiatorRule extends
		ConstructorInstantiatorRuleBase {

	public JSR330ConstructorInstantiatorRule(
			StandardInjectorConfiguration config) {
		super(config);
	}

	@Override
	protected Integer getConstructorPriority(Constructor<?> c) {
		if (c.isAnnotationPresent(Inject.class))
			return 2;
		boolean isInnerClass = c.getDeclaringClass().getEnclosingClass() != null;

		if (c.getParameterCount() == 0
				&& (Modifier.isPublic(c.getModifiers()) || isInnerClass))
			return 1;
		return null;
	}

}
