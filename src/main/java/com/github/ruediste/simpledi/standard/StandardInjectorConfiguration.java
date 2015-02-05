package com.github.ruediste.simpledi.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.InjectorConfiguration;
import com.github.ruediste.simpledi.core.Scope;
import com.github.ruediste.simpledi.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public class StandardInjectorConfiguration {
	public InjectorConfiguration config;

	public StandardInjectorConfiguration(InjectorConfiguration config) {
		this.config = config;
	}

	public Scope singletonScope;
	public Scope defaultScope;
	public final Map<Class<? extends Annotation>, Scope> scopeAnnotationMap = new HashMap<>();

	/**
	 * Strategy to instantiate an instance using a fixed constructor. The
	 * members are injected afterwards by the calling code
	 */
	public BiFunction<Constructor<?>, TypeToken<?>, Object> fixedConstructorInstantiationStrategy;

	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();
	public final List<MembersInjectorRule> membersInjectorRules = new ArrayList<>();

	/**
	 * Create an {@link RecipeInstantiator} using the {@link #instantiatorRules}
	 */
	public <T> RecipeInstantiator<T> createRecipeInstantiator(TypeToken<?> type) {
		for (InstantiatorRule rule : instantiatorRules) {
			@SuppressWarnings("unchecked")
			RecipeInstantiator<T> instantiator = (RecipeInstantiator<T>) rule
					.apply(type);
			if (instantiator != null) {
				return instantiator;
			}
		}
		return null;
	}

	public <T> RecipeInstantiator<T> createInstantiator(
			Constructor<?> constructor, TypeToken<?> type) {
		return new RecipeInstantiator<T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T instantiate(ContextualInjector injector) {
				return (T) fixedConstructorInstantiationStrategy.apply(
						constructor, type);
			}
		};

	}
}
