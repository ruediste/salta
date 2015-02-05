package com.github.ruediste.simpledi.standard.recipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.standard.util.ConstructorInstantiatorRuleBase;

/**
 * Instantiate a fixed class using a fixed constructor. Use a subclass of
 * {@link ConstructorInstantiatorRuleBase} to create an instance
 */
public class FixedConstructorRecipeInstantiator<T> implements
		RecipeInstantiator<T> {

	Constructor<?> constructor;
	List<Dependency<?>> argumentDependencies;

	public FixedConstructorRecipeInstantiator(Constructor<?> constructor,
			List<Dependency<?>> argumentDependencies) {
		constructor.setAccessible(true);
		this.constructor = constructor;
		this.argumentDependencies = argumentDependencies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T instantiate(ContextualInjector injector) {
		// resolve dependencies
		ArrayList<Object> args = new ArrayList<>();
		for (Dependency<?> dependency : argumentDependencies) {
			args.add(injector.createInstance(dependency));
		}

		// call constructor
		try {
			return (T) constructor.newInstance(args.toArray());
		} catch (InvocationTargetException e) {
			throw new ProvisionException("Error in constructor " + constructor,
					e.getCause());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException e) {
			throw new ProvisionException("Error while calling constructor", e);
		}
	}

}