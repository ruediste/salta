package com.github.ruediste.simpledi.jsr330;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.simpledi.standard.util.ConstructorInstantiatorRuleBase;

public class JSR330ConstructorInstantiatorRule extends
		ConstructorInstantiatorRuleBase {

	@Override
	protected Integer getConstructorPriority(Constructor<?> c) {
		if (c.isAnnotationPresent(Inject.class))
			return 2;
		if (c.getParameterCount() == 0 && Modifier.isPublic(c.getModifiers()))
			return 1;
		return null;
	}

}
