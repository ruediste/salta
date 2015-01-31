package com.github.ruediste.simpledi;

/**
 * Injects dependencies into the fields and methods on instances of type
 * {@code T}.
 */
public interface MemberInjector<T> {
	void injectMembers(T instance, RecursiveInjector injector);
}
