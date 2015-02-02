package com.github.ruediste.simpledi;

@Deprecated
public class InstantiationResult<T> {

	T value;
	boolean membersInjected;

	public InstantiationResult(T value, boolean membersInjected) {
		super();
		this.value = value;
		this.membersInjected = membersInjected;
	}

	public static <T> InstantiationResult<T> of(T value) {
		return new InstantiationResult<>(value, false);
	}

	public static <T> InstantiationResult<T> of(T value, boolean membersInjected) {
		return new InstantiationResult<>(value, membersInjected);
	}
}
