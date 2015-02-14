package com.github.ruediste.salta.core;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Contains the bindings which are currently beeing constructed and is used for
 * circular dependency detection.
 */
public class RecipeCreationContextImpl implements RecipeCreationContext {

	private CoreInjector coreInjector;

	public RecipeCreationContextImpl(CoreInjector coreInjector) {
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
	public <T> CreationRecipe getOrCreateRecipe(Binding binding,
			RecipeCreationContext ctx) {
		addBinding(binding);
		try {
			return binding.getOrCreateRecipe(ctx);
		} finally {
			removeBinding(binding);
		}
	}

	@Override
	public Object getInstance(CreationRecipe recipe) {
		return coreInjector.compileRecipe(recipe).get();
	}
}
