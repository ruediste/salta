package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.ProvisionException;

public class FixedMethodRecipeMembersInjector implements
		RecipeMembersInjector {

	private Method method;
	private List<CreationRecipe> argumentRecipes;

	public FixedMethodRecipeMembersInjector(Method method,
			List<CreationRecipe> argumentRecipes) {
		this.method = method;
		this.argumentRecipes = argumentRecipes;
		method.setAccessible(true);
	}

	@Override
	public void injectMembers(Object instance) {

		// resolve dependencies
		ArrayList<Object> args = new ArrayList<>();
		for (CreationRecipe dependency : argumentRecipes) {
			args.add(dependency.createInstance());
		}

		// call method
		try {
			method.invoke(instance, args.toArray());
		} catch (InvocationTargetException e) {
			throw new ProvisionException("Error in method " + method,
					e.getCause());
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ProvisionException("Error while calling constructor", e);
		}
	}

}
