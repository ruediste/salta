package com.github.ruediste.salta.core;

/**
 * Represents a binding which is created Just In Time (JIT). Such a binding
 * get's created if none of the {@link CoreInjectorConfiguration#creationRules
 * creation rules} or {@link CoreInjectorConfiguration#staticBindings static
 * bindings} matches.
 */
public abstract class JITBinding extends Binding {

}
