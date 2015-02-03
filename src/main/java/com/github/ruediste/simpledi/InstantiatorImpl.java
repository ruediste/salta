package com.github.ruediste.simpledi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Instantiator;
import com.github.ruediste.simpledi.core.ProvisionException;

/**
 * Instantiate a fixed class using a fixed constructor
 */
public class InstantiatorImpl<T> implements Instantiator<T> {

	Constructor<?> constructor;
	List<Dependency<?>> argumentDependencies;

	public InstantiatorImpl(Constructor<?> constructor,
			List<Dependency<?>> argumentDependencies) {
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
