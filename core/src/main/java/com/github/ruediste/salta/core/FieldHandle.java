package com.github.ruediste.salta.core;

public class FieldHandle {
	FieldHandle(Class<?> type, String name) {
		this.name = name;
		this.type = type;

	}

	String name;
	public Class<?> type;
}
