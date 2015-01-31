package com.github.ruediste.simpledi;

/**
 * A request to instantiate an object of a certain type.
 */
public class InstantiationRequest {
	/**
	 * Requested type and qualifiers
	 */
	public final Key<?> key;

	/**
	 * Injection point the request came from
	 */
	public final InjectionPoint injectionPoint;

	public InstantiationRequest(Key<?> key, InjectionPoint injectionPoint) {
		super();
		this.key = key;
		this.injectionPoint = injectionPoint;
	}

	public InstantiationRequest withoutInjectionPoint() {
		return new InstantiationRequest(key, null);
	}

	@Override
	public String toString() {
		return key.toString()
				+ (injectionPoint == null ? "" : (injectionPoint.toString()));
	}
}
