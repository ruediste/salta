package com.github.ruediste.salta.standard.util;

import java.util.Map;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;

import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;

/**
 * Scopes a single execution of a block of code. In contrast to
 * {@link SimpleScopeHandler}, this scope handler creates a proxy, which will
 * always delegate to the instance in the current scope. Apply this scope with a
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
 * SimpleProxyScopeHandler scopeHandler=new SimpleProxyScopeHandler();
 * bindScope(MyCustomScopeAnnotation.class, new ScopeImpl(scopeHandler));
 * bind(SimpleProxyScopeHandler.class).named("myScope").toInstance(scopeHandler);
 * </code>
 * </pre>
 * 
 * @author Jesse Wilson
 * @author Fedor Karpelevitch
 * @author Ruedi Steinmann
 */
public class SimpleProxyScopeHandler extends SimpleScopeHandlerBase {

    public SimpleProxyScopeHandler(String scopeName) {
        super(scopeName);
    }

    @Override
    public Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
            CoreDependencyKey<?> requestedKey) {

        // create the proxy right away, such that it can be reused
        // afterwards
        Object proxy = Enhancer.create(requestedKey.getRawType(),
                new Dispatcher() {

                    @Override
                    public Object loadObject() throws Exception {
                        Map<Binding, Object> scopedObjects = values.get();
                        if (scopedObjects == null) {
                            throw new RuntimeException(
                                    "Cannot access " + requestedKey
                                            + " outside of scope " + scopeName);
                        }
                        return scopedObjects.computeIfAbsent(binding,
                                b -> supplier.get());
                    }
                });

        return () -> proxy;
    }

}