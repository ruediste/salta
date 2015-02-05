package com.github.ruediste.simpledi.standard;

import java.util.function.Supplier;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.simpledi.AbstractModule;
import com.github.ruediste.simpledi.core.Binding;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.JITBinding;
import com.github.ruediste.simpledi.core.JITBindingKeyRule;
import com.github.ruediste.simpledi.core.JITBindingRule;
import com.github.ruediste.simpledi.core.JitBindingKey;
import com.github.ruediste.simpledi.core.Scope;
import com.google.common.reflect.TypeToken;

/**
 * Module to setup the infrastructure provided by the standard package
 */
public class StandardModule extends AbstractModule {

	public static final AttachedProperty<JitBindingKey, TypeToken<?>> jitBindingKeyType = new AttachedProperty<>(
			"type");

	@Override
	protected void configure() {

		StandardInjectorConfiguration config = binder().getConfiguration();

		config.config.jitBindingKeyRules.add(new JITBindingKeyRule() {

			@Override
			public void apply(Dependency<?> dependency, JitBindingKey key) {
				jitBindingKeyType.set(key, dependency.type);
			}
		});

		config.config.jitBindingRules.add(new JITBindingRule() {

			@Override
			public JITBinding apply(JitBindingKey key) {
				TypeToken<?> type = jitBindingKeyType.get(key);
				StandardJitBinding binding = new StandardJitBinding();
				binding.recipeCreationSteps.add(new FillDefaultsRecipeCreationStep(
						config, type));
				return binding;
			}
		});

		config.defaultScope = new Scope() {

			@Override
			public <T> T scope(Binding key, Supplier<T> unscoped) {
				return unscoped.get();
			}
		};
	}
}
