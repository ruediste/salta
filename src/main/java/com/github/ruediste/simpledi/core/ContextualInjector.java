package com.github.ruediste.simpledi.core;


public interface ContextualInjector {

	public <T> T createInstance(Dependency<T> key);
}
