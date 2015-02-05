package com.github.ruediste.simpledi.standard.recipe;

import java.lang.reflect.Field;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.ProvisionException;

public class FixedFieldRecipeMembersInjector<T> implements
		RecipeMembersInjector<T> {

	private Field field;
	private Dependency<?> dependency;

	public FixedFieldRecipeMembersInjector(Field field, Dependency<?> dependency) {
		this.field = field;
		field.setAccessible(true);
		this.dependency = dependency;

	}

	@Override
	public void injectMembers(T instance, ContextualInjector injector) {

		Object value = injector.createInstance(dependency);
		try {
			field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ProvisionException("Error while setting field " + field,
					e);
		}
	}

}
