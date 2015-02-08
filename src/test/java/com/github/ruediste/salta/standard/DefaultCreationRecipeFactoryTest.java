package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;

public class DefaultCreationRecipeFactoryTest {

	@Test
	public void scopeNonNull() {
		StandardInjectorConfiguration config = new StandardInjectorConfiguration(
				null, null);
		config.defaultScope = mock(Scope.class);
		DefaultCreationRecipeFactory factory = new DefaultCreationRecipeFactory(
				config, null);
		StandardCreationRecipe recipe = factory.get();
		assertNotNull(recipe.scope);
	}
}
