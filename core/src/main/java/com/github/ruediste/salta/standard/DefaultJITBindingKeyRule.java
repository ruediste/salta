package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.JITBindingKey;
import com.github.ruediste.salta.core.JITBindingKeyRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public final class DefaultJITBindingKeyRule implements JITBindingKeyRule {

    private StandardInjectorConfiguration config;
    public static final AttachedProperty<JITBindingKey, Annotation> jitBindingKeyRequiredQualifiers = new AttachedProperty<>(
            "required qualifiers");
    public static final AttachedProperty<JITBindingKey, TypeToken<?>> jitBindingKeyType = new AttachedProperty<>(
            "type");

    public DefaultJITBindingKeyRule(StandardInjectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void apply(CoreDependencyKey<?> dependency, JITBindingKey key) {
        DefaultJITBindingKeyRule.jitBindingKeyType.set(key,
                dependency.getType());
        DefaultJITBindingKeyRule.jitBindingKeyRequiredQualifiers.set(key,
                config.getRequiredQualifier(dependency));
    }
}