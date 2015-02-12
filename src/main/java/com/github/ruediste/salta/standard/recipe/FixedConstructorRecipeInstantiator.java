package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.InstantiationContext;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.TransitiveCreationRecipe;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;

/**
 * Instantiate a fixed class using a fixed constructor. Use a subclass of
 * {@link ConstructorInstantiatorRuleBase} to create an instance
 */
public class FixedConstructorRecipeInstantiator<T> implements
		RecipeInstantiator<T> {

	Constructor<?> constructor;
	List<CoreDependencyKey<?>> argumentDependencies;

	public FixedConstructorRecipeInstantiator(Constructor<?> constructor,
			List<CoreDependencyKey<?>> argumentDependencies) {
		constructor.setAccessible(true);
		this.constructor = constructor;
		this.argumentDependencies = argumentDependencies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T instantiate(ContextualInjector injector) {
		// resolve dependencies
		ArrayList<Object> args = new ArrayList<>();
		for (CoreDependencyKey<?> dependency : argumentDependencies) {
			args.add(injector.getInstance(dependency));
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

	@SuppressWarnings("unchecked")
	@Override
	public TransitiveRecipeInstantiator createTransitive(
			InstantiationContext ctx) {
		ArrayList<TransitiveCreationRecipe> argRecipes = new ArrayList<>();
		for (CoreDependencyKey<?> dependency : argumentDependencies) {
			argRecipes.add(ctx.getTransitiveRecipe(dependency));
		}

		return injector -> {
			// create dependencies
			ArrayList<Object> args = new ArrayList<>();
			for (TransitiveCreationRecipe recipe : argRecipes) {
				args.add(recipe.createInstance(injector));
			}

			// call constructor
			try {
				return constructor.newInstance(args.toArray());
			} catch (InvocationTargetException e) {
				throw new ProvisionException("Error in constructor "
						+ constructor, e.getCause());
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException e) {
				throw new ProvisionException("Error while calling constructor",
						e);
			}
		};
	}
}
