package com.github.ruediste.simpledi.core;


public interface StaticBinding {
	boolean matches(Dependency<?> dependency);

	CreationRecipe createRecipe();
}
