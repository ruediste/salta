package com.github.ruediste.salta.core.attachedProperties;

/**
 * Implementation of the {@link AttachedPropertyBearer} interface, to be used as
 * base class.
 */
public class AttachedPropertyBearerBase implements AttachedPropertyBearer {

    private final AttachedPropertyMap map = new AttachedPropertyMap();

    @Override
    public AttachedPropertyMap getAttachedPropertyMap() {
        return map;
    }

}
