package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Field;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.ProvisionException;

public class FixedFieldRecipeMembersInjector<T> implements
		RecipeMembersInjector<T> {

	private Field field;
	private CoreDependencyKey<?> dependency;

	public FixedFieldRecipeMembersInjector(Field field, CoreDependencyKey<?> dependency) {
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
