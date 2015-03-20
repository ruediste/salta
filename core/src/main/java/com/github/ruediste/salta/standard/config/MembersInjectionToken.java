package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.standard.Injector;
import com.google.common.reflect.TypeToken;

/**
 * A token which makes sure that the members of the contained value are injected
 * before the value is returned.
 */
public class MembersInjectionToken<T> {
	private Injector injector;
	private T value;
	private volatile boolean injected;
	private TypeToken<T> type;

	public MembersInjectionToken(Injector injector, T value, TypeToken<T> type) {
		this.injector = injector;
		this.value = value;
		this.type = type;
	}

	public T getValue() {
		if (!injected)
			synchronized (injector.getCoreInjector().recipeLock) {
				if (!injected) {
					injector.injectMembers(type, value);
					injected = true;
				}
			}
		return value;
	}
}