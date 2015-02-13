package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Field;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.ProvisionException;

public class FixedFieldRecipeMembersInjector implements
		RecipeMembersInjector {

	private Field field;
	private CreationRecipe recipe;

	public FixedFieldRecipeMembersInjector(Field field, CreationRecipe recipe) {
		this.field = field;
		this.recipe = recipe;
		field.setAccessible(true);

	}

	@Override
	public void injectMembers(Object instance) {
		Object value = recipe.createInstance();
		try {
			field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ProvisionException("Error while setting field " + field,
					e);
		}

	}

}
