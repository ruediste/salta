package com.github.ruediste.salta.core;

import java.util.Set;

import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public abstract class StaticBinding extends Binding {
    /**
     * If possible, return the types this binding matches. Used to optimize
     * binding lookup. The binding will never be called, if
     * {@link CoreDependencyKey#getType()} returns a type which is not in the
     * returned set.
     * 
     * <p>
     * If null or an empty set is returned, the matcher retruned from
     * {@link #getMatcher()} will be called upon every lookup.
     * </p>
     */
    public Set<TypeToken<?>> getPossibleTypes() {
        return null;
    }

    public abstract Matcher<CoreDependencyKey<?>> getMatcher();
}
