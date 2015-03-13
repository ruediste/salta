package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Base class for static members injectors.
 */
public abstract class StaticMembersInjectorBase {

	/**
	 * Determine if a field should be injected. Called for static fields only;
	 */
	protected abstract boolean shouldInject(Field field);

	/**
	 * Determine if a method should be injected. Called for static methods only;
	 */
	protected abstract boolean shouldInject(Method method);

	/**
	 * Injects all
	 * {@link StandardInjectorConfiguration#requestedStaticInjections}
	 */
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
			if (!shouldInject(f))
				continue;
			InjectionPoint<?> d = new InjectionPoint<>(TypeToken.of(f
					.getGenericType()), f, f, null);
			f.setAccessible(true);
			try {
				f.set(null, injector.getInstance(d));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new SaltaException("Error while setting static " + f, e);
			}
		}
		// inject methods
		for (Method m : cls.getDeclaredMethods()) {
			if (!Modifier.isStatic(m.getModifiers()))
				continue;
			if (!shouldInject(m))
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
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new SaltaException("Error while setting static " + m, e);
			} catch (InvocationTargetException e) {
				throw new SaltaException("Error while setting static " + m,
						e.getCause());
			}
		}
	}

}