package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CreationRecipeFactory;
import com.github.ruediste.salta.core.TransitiveCreationRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeFactory implements CreationRecipeFactory {

	private StandardInjectorConfiguration config;
	private TypeToken<?> type;

	public DefaultCreationRecipeFactory(StandardInjectorConfiguration config,
			TypeToken<?> type) {
		this.config = config;
		this.type = type;
	}

	@Override
	public StandardCreationRecipe createRecipe() {
		StandardCreationRecipe recipe = new StandardCreationRecipe();
		recipe.instantiator = config.createRecipeInstantiator(type);
		recipe.membersInjectors.addAll(config
				.createRecipeMembersInjectors(type));
		recipe.scope = config.getScope(type);
		return recipe;
	}

	@Override
	public TransitiveCreationRecipe createTransitiveDirect(
			ContextualInjector ctx) {
		// TODO Auto-generated method stub
		return null;
	}

}
