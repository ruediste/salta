package com.github.ruediste.simpledi.standard;

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

		if (recipe.scope == null)
			recipe.scope = config.defaultScope;
	}

}
