package com.github.ruediste.salta.core.attachedProperties;

import java.util.function.Supplier;

/**
 * Identifies a property which can be attached to any
 * {@link AttachedPropertyBearer}
 */
public class AttachedProperty<Bearer extends AttachedPropertyBearer, T> {

    private final String name;

    public AttachedProperty() {
        name = "<unnamed>";
    }

    public AttachedProperty(String name) {
        this.name = name;
    }

    /**
     * Return the value associated with the bearer. If the property is not set,
     * null is returned. To determine if a property is set, use
     * {@link #isSet(AttachedPropertyBearer)}
     */
    public T get(Bearer bearer) {
        return bearer.getAttachedPropertyMap().get(this);
    }

    /**
     * Set the value associated with the bearer. Note that setting a property to
     * null does not clear the property. Use
     * {@link #clear(AttachedPropertyBearer)} for that purpose.
     */
    public void set(Bearer bearer, T value) {
        bearer.getAttachedPropertyMap().set(this, value);
    }

    /**
     * Clear the property on the bearer. After calling this method,
     * {@link #isSet(AttachedPropertyBearer)} will return false.
     */
    public void clear(Bearer bearer) {
        bearer.getAttachedPropertyMap().clear(this);
    }

    /**
     * Determine if the property is set on the.
     */
    public boolean isSet(Bearer bearer) {
        return bearer.getAttachedPropertyMap().isSet(this);
    }

    /**
     * Set a property to the specified value if it is not set. Thread safe
     *
     * @return the current value of the property
     */
    public T setIfAbsent(Bearer bearer, T value) {
        synchronized (bearer.getAttachedPropertyMap()) {
            if (!isSet(bearer)) {
                set(bearer, value);
                return value;
            } else {
                return get(bearer);
            }
        }
    }

    /**
     * Set a property to the specified value if it is not set.
     *
     * @return the current value of the property
     */
    public T setIfAbsent(Bearer bearer, Supplier<T> valueSupplier) {
        synchronized (bearer.getAttachedPropertyMap()) {
            if (!isSet(bearer)) {
                T value = valueSupplier.get();
                set(bearer, value);
                return value;
            } else {
                return get(bearer);
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
