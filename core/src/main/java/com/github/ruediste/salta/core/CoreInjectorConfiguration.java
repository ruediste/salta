package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Contains the whole configuration of an injector.
 */
public class CoreInjectorConfiguration extends AttachedPropertyBearerBase {

	/**
	 * Rules to instantiate dependencies without bindings
	 */
	public final List<CreationRule> creationRules = new ArrayList<>();

	/**
	 * Statically defined bindings
	 */
	public final List<StaticBinding> staticBindings = new ArrayList<>();

	/**
	 * Automatically defined statically defined bindings. These bindings are
	 * evaluated after the {@link #staticBindings}, but before the
	 * {@link #jitBindingRules}
	 */
	public final List<StaticBinding> automaticStaticBindings = new ArrayList<>();

	/**
	 * Rules to create the key used to lookup and create JIT bindings
	 */
	public final List<JITBindingKeyRule> jitBindingKeyRules = new ArrayList<>();

	/**
	 * Rules to create JIT bindings. The rules are matched until the first
	 * returns a non null result, which will be used as jit binding
	 */
	public final List<JITBindingRule> jitBindingRules = new ArrayList<>();

	public InjectionStrategy injectionStrategy = InjectionStrategy.INVOKE_DYNAMIC;
}
