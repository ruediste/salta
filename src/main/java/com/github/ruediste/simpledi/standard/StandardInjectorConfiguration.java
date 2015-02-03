package com.github.ruediste.simpledi.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ruediste.simpledi.core.InjectorConfiguration;
import com.github.ruediste.simpledi.core.Scope;
import com.google.common.reflect.TypeToken;

public class StandardInjectorConfiguration extends InjectorConfiguration {
	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();

	public Scope singletonScope;
	public Scope defaultScope;

	public final Map<Class<? extends Annotation>, Scope> scopeAnnotationMap = new HashMap<>();

	/**
	 * Create an {@link Instantiator} using the {@link #instantiatorRules}
	 */
	public <T> Instantiator<T> createInstantiator(TypeToken<?> type) {
		for (InstantiatorRule foo : instantiatorRules) {
			@SuppressWarnings("unchecked")
			Instantiator<T> instantiator = (Instantiator<T>) foo.apply(type);
			if (instantiator != null) {
				return instantiator;
			}
		}
		return null;
	}

	public <T> Instantiator<T> createInstantiator(Constructor<?> constructor,
			TypeToken<?> type) {
		throw new UnsupportedOperationException("Not Yet Implemented");
	}
}
