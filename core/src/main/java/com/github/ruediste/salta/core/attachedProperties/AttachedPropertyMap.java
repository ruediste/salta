package com.github.ruediste.salta.core.attachedProperties;

import java.util.HashMap;

/**
 * A map for attached properties. Instances of this class can be constructed and
 * managed by classes of other packages, but most methods are not accessible,
 * since they are intended to be used through {@link AttachedProperty}
 * instances.
 *
 * <p>
 * All operations are thread safe.
 * </p>
 */
public class AttachedPropertyMap {

    private HashMap<AttachedProperty<?, ?>, Object> map = new HashMap<AttachedProperty<?, ?>, Object>();
    private boolean frozen = false;

    @SuppressWarnings("unchecked")
    synchronized <T> T get(AttachedProperty<?, T> key) {
        return (T) map.get(key);
    }

    private void checkNotFrozen() {
        if (frozen)
            throw new IllegalStateException(
                    "Attempt to modify frozen AttachedPropertyBearer");
    }

    synchronized <T> void set(AttachedProperty<?, T> key, T value) {
        checkNotFrozen();
        map.put(key, value);
    }

    synchronized void clear(AttachedProperty<?, ?> key) {
        checkNotFrozen();
        map.remove(key);
    }

    synchronized boolean isSet(AttachedProperty<?, ?> key) {
        return map.containsKey(key);
    }

	/**
	 * Clear all properties attached to this map.
	 */
    public synchronized void clearAll() {
        checkNotFrozen();
        map.clear();
    }

    @Override
    public synchronized int hashCode() {
        return map.hashCode();
    }

    public synchronized void putAll(AttachedPropertyBearer other) {
        checkNotFrozen();
        putAll(other.getAttachedPropertyMap());
    }

    public synchronized void putAll(AttachedPropertyMap other) {
        checkNotFrozen();
        map.putAll(other.map);
    }

    public synchronized boolean isFrozen() {
        return frozen;
    }

    public synchronized void freeze() {
        frozen = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        AttachedPropertyMap other = (AttachedPropertyMap) obj;

        HashMap<AttachedProperty<?, ?>, Object> otherMap;
        synchronized (other) {
            otherMap = new HashMap<>(other.map);
        }
        synchronized (this) {
            return map.equals(otherMap);
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
