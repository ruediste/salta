package com.github.ruediste.simpledi.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.github.ruediste.simpledi.core.Binding;
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

	public Scope singletonScope = new SingletonScope();
	public Scope defaultScope = new DefaultScope();
	public final Map<Class<? extends Annotation>, Scope> scopeAnnotationMap = new HashMap<>();

	/**
	 * Strategy to instantiate an instance using a fixed constructor. The
	 * members are injected afterwards by the calling code
	 */
	public BiFunction<Constructor<?>, TypeToken<?>, Object> fixedConstructorInstantiationStrategy;

	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();
	public final List<MembersInjectorRule> membersInjectorRules = new ArrayList<>();
	public final List<ScopeRule> scopeRules = new ArrayList<>();
	public final Set<Class<?>> requestedStaticInjections = new HashSet<>();

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

	private final class DefaultScope implements Scope {
		@Override
		public <T> T scope(Binding key, Supplier<T> unscoped) {
			return unscoped.get();
		}

		@Override
		public String toString() {
			return "Default";
		}
	}

	private final class SingletonScope implements Scope {
		private ConcurrentHashMap<Binding, Object> instances = new ConcurrentHashMap<>();

		@SuppressWarnings("unchecked")
		@Override
		public <T> T scope(Binding key, Supplier<T> unscoped) {
			synchronized (key) {
				if (instances.containsKey(key))
					return (T) instances.get(key);
				else {
					T value = unscoped.get();
					instances.put(key, value);
					return value;
				}
			}
		}

		@Override
		public String toString() {
			return "Singleton";
		}
	}
}
