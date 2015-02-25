package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.util.MembersInjectorFactoryBase;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.google.common.reflect.TypeToken;

public class JSR330MembersInjectorFactory extends MembersInjectorFactoryBase {

	public JSR330MembersInjectorFactory(CoreInjectorConfiguration config) {
		super(config);
	}

	@Override
	protected boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex index) {
		if (!method.isAnnotationPresent(Inject.class))
			return false;
		if (Modifier.isAbstract(method.getModifiers())
				|| Modifier.isStatic(method.getModifiers()))
			return false;
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
