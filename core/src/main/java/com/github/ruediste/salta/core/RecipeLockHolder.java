package com.github.ruediste.salta.core;

/**
 * Holder for the recipe lock. If you need to pass around the recipe lock,
 * always pass this holder instead of the lock object directly.
 * 
 * @see CoreInjector#recipeLockHolder
 */
public class RecipeLockHolder {

	private Object recipeLock;
	private Object instantiationLock;

	public RecipeLockHolder(Object recipeLock, Object instantiationLock) {
		this.recipeLock = recipeLock;
		this.instantiationLock = instantiationLock;
	}

	public Object get() {
		if (Thread.holdsLock(instantiationLock)
				&& !Thread.holdsLock(recipeLock))
			throw new SaltaException(
					"May not acquire the recipe lock while holding the instantiation lock.\n"
							+ "Do not use the Injector from constructors of injected methods!");
		return recipeLock;
	}
}
