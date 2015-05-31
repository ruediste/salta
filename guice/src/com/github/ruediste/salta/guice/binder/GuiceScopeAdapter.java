package com.github.ruediste.salta.guice.binder;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.ScopeImpl.ScopeHandler;
import com.google.inject.Scope;

public class GuiceScopeAdapter implements ScopeHandler {

    private Scope scope;

    public GuiceScopeAdapter(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
            CoreDependencyKey<?> requestedKey) {
        return doScope(supplier, binding, requestedKey);
    }

    @SuppressWarnings("unchecked")
    private <T> Supplier<T> doScope(Supplier<Object> supplier, Binding binding,
            CoreDependencyKey<?> requestedKey) {
        return scope.scope(binding, (CoreDependencyKey<T>) requestedKey,
                () -> (T) supplier.get())::get;
    }

    @Override
    public String toString() {
        return scope.toString();
    }
}
