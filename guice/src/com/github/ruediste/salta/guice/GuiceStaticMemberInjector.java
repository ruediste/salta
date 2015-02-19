package com.github.ruediste.salta.guice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.ruediste.salta.standard.util.StaticMembersInjectorBase;
import com.google.inject.Inject;

final class GuiceStaticMemberInjector extends StaticMembersInjectorBase {

	@Override
	protected boolean shouldInject(Method method) {
		return method.isAnnotationPresent(Inject.class);
	}

	@Override
	protected boolean shouldInject(Field field) {
		return field.isAnnotationPresent(Inject.class);
	}
}