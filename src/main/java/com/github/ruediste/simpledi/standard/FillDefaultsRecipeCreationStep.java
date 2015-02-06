package com.github.ruediste.simpledi.standard;

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.core.Scope;
import com.github.ruediste.simpledi.standard.recipe.RecipeCreationStep;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

public class FillDefaultsRecipeCreationStep implements RecipeCreationStep {

	private StandardInjectorConfiguration config;
	private TypeToken<?> type;

	public FillDefaultsRecipeCreationStep(StandardInjectorConfiguration config,
			TypeToken<?> type) {
		this.config = config;
		this.type = type;
	}

	@Override
	public void accept(StandardCreationRecipe recipe) {
		if (recipe.instantiator == null) {
			recipe.instantiator = config.createRecipeInstantiator(type);
		}

		for (MembersInjectorRule rule : config.membersInjectorRules) {
			rule.addMembersInjectors(type, recipe);
		}

		for (ScopeRule rule : config.scopeRules) {
			rule.configureScope(type, recipe);
		}

		if (recipe.scope == null) {
			for (Entry<Class<? extends Annotation>, Scope> entry : config.scopeAnnotationMap
					.entrySet()) {
				if (type.getRawType().isAnnotationPresent(entry.getKey())) {
					if (recipe.scope != null)
						throw new ProvisionException(
								"Multiple scope annotations present on " + type);
					recipe.scope = entry.getValue();
				}
			}
		}

		if (recipe.scope == null)
			recipe.scope = config.defaultScope;
	}

}
