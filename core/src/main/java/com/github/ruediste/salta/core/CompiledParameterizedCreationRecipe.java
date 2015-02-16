package com.github.ruediste.salta.core;

public interface CompiledParameterizedCreationRecipe {
	Object get(Object parameter) throws Throwable;

	default Object getNoThrow(Object parameter) {
		try {
			return get(parameter);
		} catch (ProvisionException e) {
			throw e;
		} catch (Throwable e) {
			throw new ProvisionException(
					"Error while getting instance from compiled recipe", e);
		}
	}
}
