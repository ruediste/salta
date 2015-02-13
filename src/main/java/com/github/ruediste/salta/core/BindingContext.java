package com.github.ruediste.salta.core;

import java.util.function.Supplier;

public interface BindingContext {

	CreationRecipe getRecipe(CoreDependencyKey<?> dependency);

	<T> T withBinding(Binding binding, Supplier<T> sup);
}