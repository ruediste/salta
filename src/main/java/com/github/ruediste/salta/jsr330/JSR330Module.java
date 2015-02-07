package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.Injector;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.StandardInjectionPoint;
import com.github.ruediste.salta.standard.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();
		config.instantiatorRules.add(new JSR330ConstructorInstantiatorRule());

		config.membersInjectorRules.add(new JSR330MembersInjectorRule());

		config.config.creationRules.add(new ProviderCreationRule());

		// register initializer for requested static injections
		config.config.dynamicInitializers.add(new Consumer<Injector>() {

			@Override
			public void accept(Injector injector) {
				Set<Class<?>> injectedClasses = new HashSet<>();
				for (Class<?> cls : config.requestedStaticInjections) {
					performStaticInjections(cls, injector, injectedClasses);
				}
			}

			private void performStaticInjections(Class<?> cls,
					Injector injector, Set<Class<?>> injectedClasses) {
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
					Dependency<?> d = new Dependency<>(TypeToken.of(f
							.getGenericType()), new StandardInjectionPoint(f,
							f, null));
					f.setAccessible(true);
					try {
						f.set(null, injector.createInstance(d));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new ProvisionException(
								"Error while setting static " + f, e);
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
						Dependency<?> d = new Dependency<>(TypeToken.of(p
								.getParameterizedType()),
								new StandardInjectionPoint(m, p, i));
						args.add(injector.createInstance(d));
					}

					m.setAccessible(true);
					try {
						m.invoke(null, args.toArray());
					} catch (IllegalArgumentException | IllegalAccessException
							| InvocationTargetException e) {
						throw new ProvisionException(
								"Error while setting static " + m, e);
					}
				}
			}
		});
		bindScope(Singleton.class, config.singletonScope);
	}
}
