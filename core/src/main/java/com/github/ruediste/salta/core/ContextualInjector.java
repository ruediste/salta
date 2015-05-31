package com.github.ruediste.salta.core;

import java.util.function.Supplier;

public interface ContextualInjector {

    public <T> T getInstance(CoreDependencyKey<T> key);

    public <T> T withBinding(Binding binding, Supplier<T> sup);
}
