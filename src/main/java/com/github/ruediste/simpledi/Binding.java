package com.github.ruediste.simpledi;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.reflect.TypeToken;

public abstract class Binding {
	public TypeToken<?> type;
	public Set<Annotation> qualifiers;

	abstract public CreationRecipe createRecipe();
}
