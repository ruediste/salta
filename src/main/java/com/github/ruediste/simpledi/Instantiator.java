package com.github.ruediste.simpledi;

public interface Instantiator<T> {

	T instantiate(InstantiationRequest request);
}
