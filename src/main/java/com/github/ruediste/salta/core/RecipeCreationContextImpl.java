package com.github.ruediste.salta.core;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

/**
 * Contains the bindings which are currently beeing constructed and is used for
 * circular dependency detection.
 */
public class RecipeCreationContextImpl implements RecipeCreationContext {

	private CoreInjector coreInjector;

	public RecipeCreationContextImpl(CoreInjector coreInjector) {
		this.coreInjector = coreInjector;
	}

	private LinkedHashSet<Binding> currentBindings = new LinkedHashSet<>();

	private ArrayList<Consumer<RecipeCreationContext>> queuedActions = new ArrayList<>();

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
	public CreationRecipe getOrCreateRecipe(Binding binding) {
		addBinding(binding);
		try {
			return binding.getOrCreateRecipe(this);
		} finally {
			removeBinding(binding);
		}
	}

	@Override
	public CompiledCreationRecipe compileRecipe(CreationRecipe recipe) {
		return coreInjector.compileRecipe(recipe);
	}

	@Override
	public CreationRecipe getRecipe(CoreDependencyKey<?> dependency) {
		return coreInjector.getRecipe(dependency, this);
	}

	@Override
	public CreationRecipe getRecipeInNewContext(CoreDependencyKey<?> dependency) {
		return coreInjector.getRecipe(dependency);
	}

	public void processQueuedActions() {
		queuedActions.forEach(x -> x.accept(this));
	}

	@Override
	public void queueAction(Consumer<RecipeCreationContext> action) {
		queuedActions.add(action);
	}

}
