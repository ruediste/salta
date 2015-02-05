package com.github.ruediste.simpledi.standard;

import java.util.function.Consumer;

import com.google.common.reflect.TypeToken;

public class FillDefaultsRecipeCreationStep implements
		Consumer<StandardCreationRecipe> {

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
			recipe.instantiator = config.createInstantiator(type);
		}
		if (recipe.scope == null)
			recipe.scope = config.defaultScope;
	}

}
