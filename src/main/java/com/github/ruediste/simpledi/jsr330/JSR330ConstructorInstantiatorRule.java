package com.github.ruediste.simpledi.jsr330;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import com.github.ruediste.simpledi.standard.util.ConstructorInstantiatorRuleBase;

public class JSR330ConstructorInstantiatorRule extends
		ConstructorInstantiatorRuleBase {

	public JSR330ConstructorInstantiatorRule() {
		super(true);
	}

	@Override
	protected boolean isInjectableConstructor(Constructor<?> c) {
		return c.isAnnotationPresent(Inject.class);
	}

}
