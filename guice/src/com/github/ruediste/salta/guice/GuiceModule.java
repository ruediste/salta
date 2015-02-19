package com.github.ruediste.salta.guice;

import java.lang.reflect.TypeVariable;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.DependencyFactoryRuleImpl;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ProviderDependencyFactoryRule;
import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class GuiceModule extends AbstractModule {

	private GuiceInjectorConfiguration guiceConfig;
	private GuiceInjectorImpl injector;

	public GuiceModule(GuiceInjectorConfiguration guiceConfig,
			GuiceInjectorImpl injector) {
		this.guiceConfig = guiceConfig;
		this.injector = injector;
	}

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();
		config.instantiatorRules.add(new GuiceConstructorInstantiatorRule());

		config.defaultMembersInjectorFactories
				.add(new GuiceMembersInjectorFactory());

		config.config.creationRules.add(new ProviderDependencyFactoryRule(
				Provider.class::equals,
				(type, supplier) -> (Provider<?>) supplier::get));

		// Rule for type literals
		config.config.creationRules
				.add(new DependencyFactoryRuleImpl(
						k -> TypeLiteral.class.equals(k.getRawType()),
						key -> {
							TypeToken<?> type = key.getType();
							if (type.getType() instanceof Class) {
								throw new ProvisionException(
										"Cannot inject a TypeLiteral that has no type parameter");
							}
							TypeToken<?> typeParameter = type
									.resolveType(TypeLiteral.class
											.getTypeParameters()[0]);
							if (typeParameter.getType() instanceof TypeVariable)
								throw new ProvisionException(
										"TypeLiteral<"
												+ typeParameter
												+ "> cannot be used as a key; It is not fully specified.");
							TypeLiteral<?> typeLiteral = TypeLiteral
									.get(typeParameter.getType());
							return () -> typeLiteral;
						}));

		bind(Injector.class).toInstance(injector);

		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new GuiceStaticMemberInjector()
				.injectStaticMembers(config, i));
		bindScope(Singleton.class, config.singletonScope);

		config.staticInitializers.add(injector::setDelegate);
		install(new JSR330Module());
	}
}
