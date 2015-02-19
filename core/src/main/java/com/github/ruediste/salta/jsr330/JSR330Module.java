package com.github.ruediste.salta.jsr330;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.standard.StandardModule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ProviderDependencyFactoryRule;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();
		config.instantiatorRules.add(new JSR330ConstructorInstantiatorRule());

		config.defaultMembersInjectorFactories
				.add(new JSR330MembersInjectorFactory());

		config.config.creationRules.add(new ProviderDependencyFactoryRule(
				Provider.class::equals,
				(type, supplier) -> (Provider<?>) supplier::get));

		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new JSR330StaticMemberInjector()
				.injectStaticMembers(config, i));

		bindScope(Singleton.class, config.singletonScope);

		install(new StandardModule());
	}
}
