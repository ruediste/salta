package com.github.ruediste.salta.core;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

/**
 * Contains the bindings which are currently beeing constructed and is used for
 * circular dependency detection.
 */
public class BindingContextImpl implements BindingContext {

	private CoreInjector coreInjector;

	public BindingContextImpl(CoreInjector coreInjector) {
		this.coreInjector = coreInjector;
	}

	@Override
	public CreationRecipe getRecipe(CoreDependencyKey<?> dependency) {
		return coreInjector.getRecipe(dependency, this);
	}

	private LinkedHashSet<Binding> currentBindings = new LinkedHashSet<>();

	private void removeBinding(Binding b) {
		currentBindings.remove(b);
	}

	private void addBinding(Binding binding) {
		if (!currentBindings.add(binding)) {
			boolean found = false;
			ArrayList<String> msg = new ArrayList<String>();
			for (Binding b : currentBindings) {
				if (b.equals(binding)) {
					found = true;
				}
				if (found)
					msg.add(b.toString());
			}
			msg.add(binding.toString());
			throw new ProvisionException("Detected Dependency Circle: "
					+ msg.stream().collect(joining("\n", "\n", "\n")));
		}
	}

	@Override
	public <T> T withBinding(Binding binding, Supplier<T> sup) {
		addBinding(binding);
		try {
			return sup.get();
		} finally {
			removeBinding(binding);
		}
	}
}
