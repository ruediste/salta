package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.FillDefaultsRecipeCreationStep;
import com.github.ruediste.salta.standard.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;

public class FillDefaultsRecipeCreationStepTest {

	@Test
	public void scopeNonNull() {
		StandardInjectorConfiguration config = new StandardInjectorConfiguration(
				null);
		config.defaultScope = mock(Scope.class);
		FillDefaultsRecipeCreationStep step = new FillDefaultsRecipeCreationStep(
				config, null);
		StandardCreationRecipe recipe = new StandardCreationRecipe();
		step.accept(recipe);
		assertNotNull(recipe.scope);
	}
}
