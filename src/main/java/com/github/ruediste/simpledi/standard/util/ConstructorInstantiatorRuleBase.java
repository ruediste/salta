package com.github.ruediste.simpledi.standard.util;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.standard.InstantiatorRule;
import com.github.ruediste.simpledi.standard.StandardInjectionPoint;
import com.github.ruediste.simpledi.standard.recipe.FixedConstructorRecipeInstantiator;
import com.github.ruediste.simpledi.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

/**
 * Base class for {@link InstantiatorRule}s which use a constructor to
 * instantiate a class (Probably the majority of cases)
 */
public abstract class ConstructorInstantiatorRuleBase implements
		InstantiatorRule {

	@Override
	public <T> RecipeInstantiator<T> apply(TypeToken<T> typeToken) {
		Type type = typeToken.getType();
		Class<?> clazz;
		if (type instanceof Class) {
			clazz = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			clazz = (Class<?>) ((ParameterizedType) type).getRawType();
		} else
			throw new ProvisionException("Unknown type " + typeToken);

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
			throw new ProvisionException(
					"Ambigous eligible constructors found on type\n"
							+ typeToken
							+ "\nConstructors:\n"
							+ highestPriorityConstructors.stream()
									.map(Object::toString)
									.collect(joining("\n->", "->", "")));

		if (highestPriorityConstructors.isEmpty()) {
			throw new ProvisionException(
					"No suitable constructor found for type " + typeToken);
		}

		Constructor<?> constructor = highestPriorityConstructors.get(0);

		ArrayList<Dependency<?>> args = new ArrayList<>();

		Parameter[] parameters = constructor.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Dependency<Object> dependency = new Dependency<Object>(
					(TypeToken) typeToken.resolveType(parameter
							.getParameterizedType()),
					new StandardInjectionPoint(constructor, parameter, i));
			args.add(dependency);
		}

		return new FixedConstructorRecipeInstantiator<T>(constructor, args);
	}

	/**
	 * Calculate the priority of the constructor. if null is returned, the
	 * constructor is not used for injection. Otherwise the constructor with the
	 * highest priority is used (highest number). If multiple highest-priority
	 * constructors exist, an error is raised.
	 */
	protected abstract Integer getConstructorPriority(Constructor<?> c);

}
