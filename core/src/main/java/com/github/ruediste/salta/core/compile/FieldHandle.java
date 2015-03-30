package com.github.ruediste.salta.core.compile;

public class FieldHandle {
	FieldHandle(Class<?> type, String name) {
		this.name = name;
		this.type = type;

	}

	String name;
	public Class<?> type;

	public String getName() {
		return name;
	}
}
