package com.github.ruediste.simpledi;

public interface RecursiveInjector {

	public <T> T createInstance(InstantiationRequest request);
}
