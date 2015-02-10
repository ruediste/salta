package com.github.ruediste.salta.core;

public interface ContextualInjector {

	public <T> T getInstance(CoreDependencyKey<T> key);

	public CoreInjector getInjector();

	public InstantiationContext getInstantiationContext();
}
