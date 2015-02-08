package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeFactory implements Supplier<CreationRecipe> {

	private StandardInjectorConfiguration config;
	private TypeToken<?> type;

	public DefaultCreationRecipeFactory(StandardInjectorConfiguration config,
			TypeToken<?> type) {
		this.config = config;
		this.type = type;
	}

	@Override
	public StandardCreationRecipe get() {
		StandardCreationRecipe recipe = new StandardCreationRecipe();
		recipe.instantiator = config.createRecipeInstantiator(type);
		recipe.membersInjectors.addAll(config
				.createRecipeMembersInjectors(type));
		recipe.scope = config.getScope(type);
		return recipe;
	}

}
