package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import com.github.ruediste.salta.standard.util.StaticMembersInjectorBase;

final class JSR330StaticMemberInjector extends StaticMembersInjectorBase {

	@Override
	protected boolean shouldInject(Method method) {
		return method.isAnnotationPresent(Inject.class);
	}

	@Override
	protected boolean shouldInject(Field field) {
		return field.isAnnotationPresent(Inject.class);
	}
}