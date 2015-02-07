package com.github.ruediste.salta.core;

import com.google.common.reflect.TypeToken;

public interface ContextualInjector {

	public <T> T createInstance(Dependency<T> key);

	public void injectMembers(TypeToken<?> type, Object value);

	public Injector getInjector();
}
