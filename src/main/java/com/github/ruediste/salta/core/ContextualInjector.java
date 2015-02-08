package com.github.ruediste.salta.core;


public interface ContextualInjector {

	public <T> T createInstance(CoreDependencyKey<T> key);

	public CoreInjector getInjector();
}
