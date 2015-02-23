package com.github.ruediste.salta.core;

public interface CompiledFunction {
	Object get(Object parameter) throws Throwable;

	default Object getNoThrow(Object parameter) {
		try {
			return get(parameter);
		} catch (SaltaException e) {
			throw e;
		} catch (Throwable e) {
			throw new SaltaException(
					"Error while getting instance from compiled recipe", e);
		}
	}
}
