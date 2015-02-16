package com.github.ruediste.salta.standard;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.JITBindingKey;
import com.github.ruediste.salta.core.JITBindingKeyRule;
import com.github.ruediste.salta.core.JITBindingRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Module to setup the infrastructure provided by the standard package
 */
public class StandardModule extends AbstractModule {

	public static final AttachedProperty<JITBindingKey, TypeToken<?>> jitBindingKeyType = new AttachedProperty<>(
			"type");

	@Override
	protected void configure() {

		StandardInjectorConfiguration config = binder().getConfiguration();

		config.config.jitBindingKeyRules.add(new JITBindingKeyRule() {

			@Override
			public void apply(CoreDependencyKey<?> dependency, JITBindingKey key) {
				jitBindingKeyType.set(key, dependency.getType());
			}
		});

		config.config.jitBindingRules.add(new JITBindingRule() {

			@Override
			public JITBinding apply(JITBindingKey key) {
				TypeToken<?> type = jitBindingKeyType.get(key);
				StandardJitBinding binding = new StandardJitBinding(type);
				binding.recipeFactory = ctx -> new DefaultCreationRecipeBuilder(
						config, type, binding).build(ctx);
				return binding;
			}
		});

	}
}
