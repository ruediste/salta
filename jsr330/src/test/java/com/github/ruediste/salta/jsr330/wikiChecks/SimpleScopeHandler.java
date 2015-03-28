package com.github.ruediste.salta.jsr330.wikiChecks;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.ScopeImpl.ScopeHandler;
import com.google.common.collect.Maps;

/**
 * Scopes a single execution of a block of code. Apply this scope with a
 * try/finally block:
 * 
 * <pre>
 * <code>
 * 
 *   scopeHandler.enter();
 *   try {
 *     // create and access scoped objects
 *   } finally {
 *     scopeHandler.exit();
 *   }
 * </code>
 * </pre>
 *
 * Register it with
 * 
 * <pre>
 * <code>
 * bindScope(MyCustomScopeAnnotation.class, new ScopeImpl(scopeHandler));
 * </code>
 * </pre>
 * 
 * @author Jesse Wilson
 * @author Fedor Karpelevitch
 * @author Ruedi Steinmann
 */
public class SimpleScopeHandler implements ScopeHandler {

	private final ThreadLocal<Map<Binding, Object>> values = new ThreadLocal<>();

	public void enter() {
		checkState(values.get() == null,
				"A scoping block is already in progress");
		values.set(Maps.newHashMap());
	}

	public void exit() {
		checkState(values.get() != null, "No scoping block in progress");
		values.remove();
	}

	@Override
	public Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
			CoreDependencyKey<?> requestedKey) {

		return () -> {
			Map<Binding, Object> scopedObjects = getScopedObjectMap(requestedKey);
			if (!scopedObjects.containsKey(binding)) {
				Object current = supplier.get();
				scopedObjects.put(binding, current);
				return current;
			} else
				return scopedObjects.get(binding);

		};
	}

	private Map<Binding, Object> getScopedObjectMap(
			CoreDependencyKey<?> requestedKey) {
		Map<Binding, Object> scopedObjects = values.get();
		if (scopedObjects == null) {
			throw new RuntimeException("Cannot access " + requestedKey
					+ " outside of a scoping block");
		}
		return scopedObjects;
	}

}