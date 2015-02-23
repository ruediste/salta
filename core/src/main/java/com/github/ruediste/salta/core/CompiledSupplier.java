package com.github.ruediste.salta.core;

public interface CompiledSupplier {
	Object get() throws Throwable;

	default Object getNoThrow() {
		try {
			return get();
		} catch (SaltaException e) {
			throw e;
		} catch (Throwable e) {
			throw new SaltaException(
					"Error while getting instance from compiled recipe", e);
		}
	}
}
