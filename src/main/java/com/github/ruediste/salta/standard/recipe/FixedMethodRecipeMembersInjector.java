package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.ProvisionException;

public class FixedMethodRecipeMembersInjector<T> implements
		RecipeMembersInjector<T> {

	private Method method;
	private ArrayList<CoreDependencyKey<?>> argumentDependencies;

	public FixedMethodRecipeMembersInjector(Method method,
			ArrayList<CoreDependencyKey<?>> argumentDependencies) {
		this.method = method;
		this.argumentDependencies = argumentDependencies;
		method.setAccessible(true);

	}

	@Override
	public void injectMembers(T instance, ContextualInjector injector) {

		// resolve dependencies
		ArrayList<Object> args = new ArrayList<>();
		for (CoreDependencyKey<?> dependency : argumentDependencies) {
			args.add(injector.getInstance(dependency));
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
