package com.github.ruediste.salta.core;

import java.util.function.Supplier;

public class Foo {

	Object recipeOuter() {
		Object instance = wrapper(() -> recipeInner());
		return instance;
	}

	Object recipeInner() {
		return null;
	}

	Object wrapper(Supplier<Object> inner) {
		return null;
	}
}
