package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.ProviderMethodBinder;
import com.github.ruediste.salta.standard.StandardModule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ProviderDependencyFactoryRule;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();
		config.instantiatorRules.add(new JSR330ConstructorInstantiatorRule());
		config.noInstantiatorFoundErrorProducers
				.add(typeToken -> {
					Class<?> clazz = typeToken.getRawType();
					Class<?> enclosingClass = clazz.getEnclosingClass();
					if (enclosingClass != null) {
						for (Constructor<?> c : clazz.getDeclaredConstructors()) {
							if (c.getParameterCount() == 1
									&& enclosingClass.equals(c
											.getParameterTypes()[0])) {
								throw new ProvisionException(
										"No suitable constructor found for inner non-static class "
												+ typeToken
												+ ".\nCannot instantiate non-static inner classes. Forgotten to make class static?");
							}
						}
					}
				});
		config.defaultMembersInjectorFactories
				.add(new JSR330MembersInjectorFactory());

		config.config.creationRules.add(new ProviderDependencyFactoryRule(
				key -> {
					return key.getType().getRawType().equals(Provider.class);
				}, (type, supplier) -> (Provider<?>) supplier::get,
				Provider.class));

		config.requiredQualifierExtractors.add(key -> Arrays.stream(
				key.getAnnotatedElement().getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(Qualifier.class)));

		config.availableQualifierExtractors.add(annotated -> Arrays.stream(
				annotated.getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(Qualifier.class)));

		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new JSR330StaticMemberInjector()
				.injectStaticMembers(config, i));

		// register scanner for provides methods
		{
			ProviderMethodBinder b = new ProviderMethodBinder(config) {

				@Override
				protected boolean isProviderMethod(Method m) {
					if (!m.isAnnotationPresent(Provides.class)) {
						return false;
					}
					if (void.class.equals(m.getReturnType())) {
						throw new ProvisionException(
								"@Provides method returns void: " + m);
					}
					return true;
				}
			};
			config.modulePostProcessors.add(b::bindProviderMethodsOf);
		}
		bindScope(Singleton.class, config.singletonScope);

		install(new StandardModule());
	}
}
