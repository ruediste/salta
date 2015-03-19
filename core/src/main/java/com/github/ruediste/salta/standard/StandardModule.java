package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.JITBindingKey;
import com.github.ruediste.salta.core.JITBindingKeyRule;
import com.github.ruediste.salta.core.JITBindingRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.config.DefaultConstructionRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Module to setup the infrastructure provided by the standard package
 */
public class StandardModule extends AbstractModule {

	public static final AttachedProperty<JITBindingKey, TypeToken<?>> jitBindingKeyType = new AttachedProperty<>(
			"type");
	public static final AttachedProperty<JITBindingKey, Annotation> jitBindingKeyRequiredQualifiers = new AttachedProperty<>(
			"required qualifiers");

	@Override
	protected void configure() {

		StandardInjectorConfiguration config = binder().getConfiguration();

		// stage creation rule
		config.config.creationRules.add(new CreationRuleImpl(key -> Stage.class
				.equals(key.getRawType()), key -> () -> config.stage));

		config.config.jitBindingKeyRules.add(new JITBindingKeyRule() {

			@Override
			public void apply(CoreDependencyKey<?> dependency, JITBindingKey key) {
				jitBindingKeyType.set(key, dependency.getType());
				jitBindingKeyRequiredQualifiers.set(key,
						config.getRequiredQualifier(dependency));
			}
		});

		config.config.jitBindingRules.add(new JITBindingRule() {

			@Override
			public JITBinding apply(JITBindingKey key) {
				TypeToken<?> type = jitBindingKeyType.get(key);
				if (!config.doQualifiersMatch(
						jitBindingKeyRequiredQualifiers.get(key),
						config.getAvailableQualifier(type.getRawType())))
					return null;

				Optional<Function<RecipeCreationContext, SupplierRecipe>> recipe = config
						.createConstructionRecipe(type).map(
								seed -> ctx -> DefaultCreationRecipeBuilder
										.applyEnhancers(seed.apply(ctx), config
												.createEnhancers(ctx, type)));
				if (!recipe.isPresent())
					return null;

				StandardJitBinding binding = new StandardJitBinding(type);
				binding.recipeFactory = recipe.get()::apply;
				binding.scopeSupplier = () -> config.getScope(type);
				return binding;
			}

		});

		config.constructionRules.add(new DefaultConstructionRule(config));
	}
}
