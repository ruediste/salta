package com.github.ruediste.salta.standard.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.standard.ScopeImpl.ScopeHandler;
import com.google.common.collect.Maps;

public abstract class SimpleScopeManagerBase implements ScopeHandler {

    protected final ThreadLocal<Map<Binding, Object>> values = new ThreadLocal<>();
    protected final String scopeName;

    public SimpleScopeManagerBase(String scopeName) {
        this.scopeName = scopeName;
    }

    public void enter(Map<Binding, Object> instances) {
        checkState(values.get() == null,
                "A scoping block is already in progress");
        values.set(instances);
    }

    /**
     * Return the current value map.
     * 
     * @throws RuntimeException
     *             if no scope is active
     */
    public Map<Binding, Object> getValueMap() {
        Map<Binding, Object> scopedObjects = values.get();
        if (scopedObjects == null) {
            throw new RuntimeException(
                    "Cannot access value map outside of scope " + scopeName);
        }
        return scopedObjects;
    }

    public void enter() {
        checkState(values.get() == null,
                "A scoping block is already in progress");
        values.set(Maps.newHashMap());
    }

    /**
     * Exit the scope
     */
    public Map<Binding, Object> exit() {
        Map<Binding, Object> currentValues = values.get();
        checkState(currentValues != null, "No scoping block in progress");
        values.remove();
        return currentValues;
    }
}