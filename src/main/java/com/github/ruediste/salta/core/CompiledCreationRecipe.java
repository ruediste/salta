package com.github.ruediste.salta.core;

public interface CompiledCreationRecipe {
	Object get() throws Throwable;

	default Object getNoThrow() {
		try {
			return get();
		} catch (ProvisionException e) {
			throw e;
		} catch (Throwable e) {
			throw new ProvisionException(
					"Error while getting instance from compiled recipe", e);
		}
	}
}
