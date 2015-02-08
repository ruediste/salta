package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

final class StaticMemberInjector {

	public void injectStaticMembers(StandardInjectorConfiguration config,
			Injector injector) {
		Set<Class<?>> injectedClasses = new HashSet<>();
		for (Class<?> cls : config.requestedStaticInjections) {
			performStaticInjections(cls, injector, injectedClasses);
		}
	}

	private void performStaticInjections(Class<?> cls, Injector injector,
			Set<Class<?>> injectedClasses) {
		if (cls == null)
			return;
		if (injectedClasses.add(cls)) {
			performStaticInjections(cls.getSuperclass(), injector,
					injectedClasses);

			performStaticInjections(injector, cls);
		}
	}

	private void performStaticInjections(Injector injector, Class<?> cls) {
		// inject fields
		for (Field f : cls.getDeclaredFields()) {
			if (!Modifier.isStatic(f.getModifiers()))
				continue;
			if (!f.isAnnotationPresent(Inject.class))
				continue;
			InjectionPoint<?> d = new InjectionPoint<>(TypeToken.of(f
					.getGenericType()), f, f, null);
			f.setAccessible(true);
			try {
				f.set(null, injector.getInstance(d));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ProvisionException("Error while setting static " + f,
						e);
			}
		}
		// inject methods
		for (Method m : cls.getDeclaredMethods()) {
			if (!Modifier.isStatic(m.getModifiers()))
				continue;
			if (!m.isAnnotationPresent(Inject.class))
				continue;
			ArrayList<Object> args = new ArrayList<>();
			Parameter[] parameters = m.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				Parameter p = parameters[i];
				CoreDependencyKey<?> d = new InjectionPoint<>(TypeToken.of(p
						.getParameterizedType()), m, p, i);
				args.add(injector.getInstance(d));
			}

			m.setAccessible(true);
			try {
				m.invoke(null, args.toArray());
			} catch (IllegalArgumentException | IllegalAccessException
					| InvocationTargetException e) {
				throw new ProvisionException("Error while setting static " + m,
						e);
			}
		}
	}
}