package com.github.ruediste.simpledi;

public interface Instantiator<T> {

	InstantiationResult<T> instantiate(InstanceRequest<T> key, ContextualInjector injector);
}
