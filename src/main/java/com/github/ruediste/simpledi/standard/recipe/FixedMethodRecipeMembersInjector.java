package com.github.ruediste.simpledi.standard.recipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.ProvisionException;

public class FixedMethodRecipeMembersInjector<T> implements
		RecipeMembersInjector<T> {

	private Method method;
	private ArrayList<Dependency<?>> argumentDependencies;

	public FixedMethodRecipeMembersInjector(Method method,
			ArrayList<Dependency<?>> argumentDependencies) {
		this.method = method;
		this.argumentDependencies = argumentDependencies;
		method.setAccessible(true);

	}

	@Override
	public void injectMembers(T instance, ContextualInjector injector) {

		// resolve dependencies
		ArrayList<Object> args = new ArrayList<>();
		for (Dependency<?> dependency : argumentDependencies) {
			args.add(injector.createInstance(dependency));
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
