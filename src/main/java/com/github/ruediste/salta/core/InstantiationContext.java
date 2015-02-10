package com.github.ruediste.salta.core;

import java.util.HashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import com.google.common.reflect.TypeToken;

/**
 * Contains the instances which are currently beeing constructed and is used for
 * circular dependency detection.
 */
public class InstantiationContext {

	private static class Entry {
		Class<?> type;
		boolean instanceSet;
		Object instance;
		Object proxy;
	}

	public final ContextualInjector injector;

	private HashMap<Binding, Entry> currentBindings = new HashMap<>();

	public InstantiationContext(CoreInjector coreInjector) {
		this.injector = new ContextualInjectorImpl(coreInjector, this);
	}

	public void removeBinding(Binding b) {
		currentBindings.remove(b);
	}

	public boolean isCircular(Binding b) {
		return currentBindings.containsKey(b);
	}

	/**
	 * Add a binding to the context
	 * 
	 * @param type
	 *            type of the dependency. Used to create a circular proxy if
	 *            necessary
	 * @param b
	 *            the binding to add
	 */
	public void addBinding(Class<?> type, Binding b) {
		if (isCircular(b))
			throw new ProvisionException(
					"Should Not Happen. Use isCircular() to check");
		Entry e = new Entry();
		e.type = type;
		currentBindings.put(b, e);
	}

	public void setInstance(Binding b, Object instance) {
		Entry entry = currentBindings.get(b);
		if (entry == null)
			throw new ProvisionException(
					"Should Not Happen. Cannot set instance of binding without adding it to the context before");
		if (entry.instanceSet)
			throw new ProvisionException(
					"Should Not Happen. Cannot set instance of binding multiple times");
		entry.instanceSet = true;
		entry.instance = instance;
	}

	public Object getInstanceOrProxy(Binding b) {
		Entry entry = currentBindings.get(b);
		if (entry == null)
			throw new ProvisionException(
					"Should Not Happen. Cannot get instance of binding without adding it to the context before");
		System.out.println("getInstOrProxy: " + entry.instance);
		if (entry.instanceSet)
			return entry.instance;

		if (entry.proxy == null) {
			// create proxy
			Enhancer e = new Enhancer();
			e.setSuperclass(entry.type);
			e.setCallback(new LazyLoader() {

				@Override
				public Object loadObject() throws Exception {
					if (!entry.instanceSet)
						throw new ProvisionException(
								"Intance underlying the circular proxy is not yet created. The proxy is accessed from the constructor");
					if (entry.instance == null)
						throw new NullPointerException(
								"The proxied instance resolved to null");
					return entry.instance;
				}
			});
			entry.proxy = e.create();
		}

		return entry.proxy;
	}

	private <T> T createInstance(Binding binding, CreationRecipe<T> recipe) {
		T result = recipe.createInstance(injector);
		setInstance(binding, result);
		recipe.injectMembers(result, injector);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Binding binding, TypeToken<?> type,
			CreationRecipe<T> recipe) {

		if (isCircular(binding))
			return (T) getInstanceOrProxy(binding);
		addBinding(type.getRawType(), binding);
		try {

			return recipe.scope.scope(binding,
					() -> createInstance(binding, recipe));
		} finally {
			removeBinding(binding);
		}
	}

}
