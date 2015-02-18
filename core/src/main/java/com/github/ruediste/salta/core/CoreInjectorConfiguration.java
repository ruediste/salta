package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.salta.standard.Module;

/**
 * Contains the whole configuration of an injector. Passed to all {@link Module}
 * s of an injector in order to be initialized.
 * 
 */
public class CoreInjectorConfiguration extends AttachedPropertyBearerBase {

	/**
	 * Rules to instantiate dependencies without bindings
	 */
	public final List<DependencyFactoryRule> creationRules = new ArrayList<>();

	/**
	 * Statically defined bindings
	 */
	public final List<StaticBinding> staticBindings = new ArrayList<>();

	/**
	 * Rules to create the key used to lookup and create JIT bindings
	 */
	public final List<JITBindingKeyRule> jitBindingKeyRules = new ArrayList<>();

	/**
	 * Rules to create JIT bindings
	 */
	public final List<JITBindingRule> jitBindingRules = new ArrayList<>();

}