package com.github.ruediste.salta.standard.util;

import java.util.Map;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;

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
 * SimpleScopeHandler scopeHandler=new SimpleScopeHandler();
 * bindScope(MyCustomScopeAnnotation.class, new ScopeImpl(scopeHandler));
 * bind(SimpleScopeHandler.class).named("myScope").toInstance(scopeHandler);
 * </code>
 * </pre>
 * 
 * @author Jesse Wilson
 * @author Fedor Karpelevitch
 * @author Ruedi Steinmann
 */
public class SimpleScopeManager extends SimpleScopeManagerBase {

    public SimpleScopeManager(String scopeName) {
        super(scopeName);
    }

    @Override
    public Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
            CoreDependencyKey<?> requestedKey) {

        return () -> {
            Map<Binding, Object> scopedObjects = tryGetValueMap()
                    .orElseThrow(() -> new RuntimeException("Cannot access "
                            + requestedKey + " outside of scope " + scopeName));
            if (!scopedObjects.containsKey(binding)) {
                Object current = supplier.get();
                scopedObjects.put(binding, current);
                return current;
            } else
                return scopedObjects.get(binding);

        };
    }

}