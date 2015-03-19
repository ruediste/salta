package com.github.ruediste.salta.core;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

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

	private ArrayList<Runnable> queuedActions = new ArrayList<>();
	private boolean closed;

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
			throw new SaltaException("Detected Dependency Circle: "
					+ msg.stream().collect(joining("\n", "\n", "\n")));
		}
	}

	@Override
	public <T> T withBinding(Binding binding, Supplier<T> supplier) {
		addBinding(binding);
		try {
			return supplier.get();
		} finally {
			removeBinding(binding);
		}
	}

	@Override
	public SupplierRecipe getRecipe(CoreDependencyKey<?> dependency) {
		return coreInjector.getRecipe(dependency, this);
	}

	private void requireNotClosed() {
		if (closed)
			throw new SaltaException("RecipeCreationContext already closed");
	}

	public void processQueuedActions() {
		requireNotClosed();
		while (!queuedActions.isEmpty()) {
			ArrayList<Runnable> tmp = new ArrayList<Runnable>(queuedActions);
			queuedActions.clear();
			for (Runnable r : tmp)
				r.run();
		}
		closed = true;
	}

	@Override
	public void queueAction(Runnable action) {
		requireNotClosed();
		queuedActions.add(action);
	}

	@Override
	public RecipeCompiler getCompiler() {
		return coreInjector.getCompiler();
	}

	@Override
	public Object getRecipeLock() {
		return coreInjector.recipeLock;
	}

	@Override
	public Optional<SupplierRecipe> tryGetRecipe(CoreDependencyKey<?> dependency) {
		return coreInjector.tryGetRecipeFunc(dependency)
				.map(f -> f.apply(this));
	}

	@Override
	public Optional<Function<RecipeCreationContext, SupplierRecipe>> tryGetRecipeFunc(
			CoreDependencyKey<?> dependency) {
		return coreInjector.tryGetRecipeFunc(dependency);
	}

}
