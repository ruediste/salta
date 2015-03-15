package com.github.ruediste.salta.guice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.MembersInjectorFactoryBase;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

public class GuiceMembersInjectorFactory extends MembersInjectorFactoryBase {

	public GuiceMembersInjectorFactory(StandardInjectorConfiguration config) {
		super(config);
	}

	@Override
	protected InjectionInstruction getInjectionInstruction(
			TypeToken<?> declaringType, Method method, MethodOverrideIndex index) {
		Inject inject = method.getAnnotation(Inject.class);
		if (inject == null)
			return InjectionInstruction.NO_INJECTION;

		if (Modifier.isAbstract(method.getModifiers()))
			throw new SaltaException(
					"Method annotated with @Inject is abstract: " + method);
		if (method.getTypeParameters().length > 0) {
			throw new SaltaException(
					"Method is annotated with @Inject but declares type parameters. Method:\n"
							+ method);
		}
		if (index.isOverridden(method))
			return InjectionInstruction.NO_INJECTION;

		return inject.optional() ? InjectionInstruction.INJECT_OPTIONAL
				: InjectionInstruction.INJECT;
	}

	@Override
	protected InjectionInstruction getInjectionInstruction(
			TypeToken<?> declaringType, Field f) {
		Inject inject = f.getAnnotation(Inject.class);
		if (inject == null)
			return InjectionInstruction.NO_INJECTION;

		if (Modifier.isFinal(f.getModifiers())) {
			throw new SaltaException("Final field annotated with @Inject");
		}
		if (Modifier.isStatic(f.getModifiers()))
			return InjectionInstruction.NO_INJECTION;

		return inject.optional() ? InjectionInstruction.INJECT_OPTIONAL
				: InjectionInstruction.INJECT;
	}
}
