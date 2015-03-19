package com.github.ruediste.salta.guice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.ruediste.salta.standard.util.StaticMembersInjectorBase;
import com.google.inject.Inject;

final class GuiceStaticMemberInjector extends StaticMembersInjectorBase {

	@Override
	protected InjectionInstruction shouldInject(Method method) {
		Inject inject = method.getAnnotation(Inject.class);
		if (inject == null) {
			return InjectionInstruction.NO_INJECT;
		}
		return inject.optional() ? InjectionInstruction.INJECT_OPTIONAL
				: InjectionInstruction.INJECT;
	}

	@Override
	protected InjectionInstruction shouldInject(Field field) {
		Inject inject = field.getAnnotation(Inject.class);
		if (inject == null) {
			return InjectionInstruction.NO_INJECT;
		}
		return inject.optional() ? InjectionInstruction.INJECT_OPTIONAL
				: InjectionInstruction.INJECT;
	}
}