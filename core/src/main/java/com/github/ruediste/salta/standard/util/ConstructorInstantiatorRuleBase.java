package com.github.ruediste.salta.standard.util;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Optional;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.config.InstantiatorRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedConstructorRecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Base class for {@link InstantiatorRule}s which use a constructor to
 * instantiate a class (Probably the majority of cases)
 */
public abstract class ConstructorInstantiatorRuleBase implements
		InstantiatorRule {

	private StandardInjectorConfiguration config;

	public ConstructorInstantiatorRuleBase(StandardInjectorConfiguration config) {
		this.config = config;

	}

	@Override
	public Optional<RecipeInstantiator> apply(RecipeCreationContext ctx,
			TypeToken<?> typeToken) {

		Type type = typeToken.getType();

		Class<?> clazz;
		if (type instanceof Class) {
			clazz = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			clazz = (Class<?>) ((ParameterizedType) type).getRawType();
		} else
			return Optional.empty();

		if (clazz.isInterface()) {
			return Optional.empty();
		}
		if (Modifier.isAbstract(clazz.getModifiers())) {
			return Optional.empty();
		}
		ArrayList<Constructor<?>> highestPriorityConstructors = new ArrayList<>();
		Integer highestPriority = null;
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (Modifier.isStatic(c.getModifiers()))
				continue;

			Integer constructorPriority = getConstructorPriority(c);

			if (constructorPriority == null)
				// constructor should not be used
				continue;

			if (highestPriority != null
					&& highestPriority > constructorPriority)
				// highest priority is higher than priority of current
				// constructor
				continue;

			if (highestPriority == null
					|| highestPriority < constructorPriority) {
				// we have to increase the priority
				highestPriority = constructorPriority;
			}

			highestPriorityConstructors.add(c);
		}
		if (highestPriorityConstructors.size() > 1)
			throw multipleConstructorsFound(typeToken, clazz,
					highestPriorityConstructors);

		if (highestPriorityConstructors.isEmpty()) {
			return Optional.empty();
		}

		Constructor<?> constructor = highestPriorityConstructors.get(0);
		if (config.getRequiredQualifier(constructor, constructor) != null) {
			throw new SaltaException("Qualifier specified on " + constructor
					+ ".\nSpecify qualifiers on parameters instead");
		}
		return FixedConstructorRecipeInstantiator.of(typeToken, ctx,
				constructor, config.config.injectionStrategy);

	}

	protected SaltaException multipleConstructorsFound(TypeToken<?> typeToken,
			Class<?> clazz,
			ArrayList<Constructor<?>> highestPriorityConstructors) {
		return new SaltaException(
				"Ambigous eligible constructors found on type\n"
						+ typeToken
						+ "\nConstructors:\n"
						+ highestPriorityConstructors.stream()
								.map(Object::toString)
								.collect(joining("\n->", "->", "")));
	}

	/**
	 * Calculate the priority of the constructor. if null is returned, the
	 * constructor is not used for injection. Otherwise the constructor with the
	 * highest priority is used (highest number). If multiple highest-priority
	 * constructors exist, an error is raised.
	 */
	protected abstract Integer getConstructorPriority(Constructor<?> c);

}
