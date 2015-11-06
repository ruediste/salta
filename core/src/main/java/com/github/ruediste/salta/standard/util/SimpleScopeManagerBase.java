package com.github.ruediste.salta.standard.util;

import java.util.Map;
import java.util.Optional;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.standard.ScopeImpl.ScopeHandler;
import com.google.common.collect.Maps;

public abstract class SimpleScopeManagerBase implements ScopeHandler {

    protected final ThreadLocal<ScopeState> currentState = new ThreadLocal<>();
    protected final String scopeName;

    public SimpleScopeManagerBase(String scopeName) {
        this.scopeName = scopeName;
    }

    public static class ScopeState {
        Map<Binding, Object> data = Maps.newHashMap();

        ScopeState() {
        }
    }

    public void inScopeDo(ScopeState state, Runnable runnable) {
        ScopeState old = setState(state);
        try {
            runnable.run();
        } finally {
            setState(old);
        }
    }

    public Optional<Map<Binding, Object>> tryGetValueMap() {
        return Optional.ofNullable(currentState.get()).map(d -> d.data);
    }

    /**
     * Return the current value map.
     * 
     * @throws RuntimeException
     *             if no scope is active
     */
    public Map<Binding, Object> getValueMap() {
        return tryGetValueMap().orElseThrow(() -> new RuntimeException(
                "Cannot access value map outside of scope " + scopeName));
    }

    /**
     * Set a fresh scope state.
     * 
     * @return the old scope state
     */
    public ScopeState setFreshState() {
        return setState(createFreshState());
    }

    public ScopeState createFreshState() {
        return new ScopeState();
    }

    /**
     * Set a given scope state
     * 
     * @param state
     *            state to set, or null to set to an empty state
     * @return the old scope state
     */
    public ScopeState setState(ScopeState state) {
        ScopeState old = currentState.get();
        if (state == null)
            currentState.remove();
        else
            currentState.set(state);
        return old;
    }
}