package com.github.ruediste.simpledi;

public interface ContextualInjector {

	public <T> T createInstance(InstanceRequest<T> key);
}
