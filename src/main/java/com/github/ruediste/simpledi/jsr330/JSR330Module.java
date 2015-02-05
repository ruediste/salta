package com.github.ruediste.simpledi.jsr330;

import com.github.ruediste.simpledi.AbstractModule;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();
		config.instantiatorRules.add(new JSR330ConstructorInstantiatorRule());
		config.membersInjectorRules.add(new JSR330FieldMembersInjectorRule());
		config.membersInjectorRules.add(new JSR330MethodMembersInjectorRule());
	}

}
