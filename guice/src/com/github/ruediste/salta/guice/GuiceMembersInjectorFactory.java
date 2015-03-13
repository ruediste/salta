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
	protected boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex index) {
		if (!method.isAnnotationPresent(Inject.class))
			return false;
		if (Modifier.isAbstract(method.getModifiers()))
			throw new SaltaException(
					"Method annotated with @Inject is abstract: " + method);
		if (method.getTypeParameters().length > 0) {
			throw new SaltaException(
					"Method is annotated with @Inject but declares type parameters. Method:\n"
							+ method);
		}
		if (index.isOverridden(method))
			return false;
		return true;
	}

	@Override
	protected boolean isInjectableField(TypeToken<?> declaringType, Field f) {
		boolean annotationPresent = f.isAnnotationPresent(Inject.class);
		if (annotationPresent && Modifier.isFinal(f.getModifiers())) {
			throw new SaltaException("Final field annotated with @Inject");
		}
		if (Modifier.isStatic(f.getModifiers()))
			return false;
		return annotationPresent;
	}
}
