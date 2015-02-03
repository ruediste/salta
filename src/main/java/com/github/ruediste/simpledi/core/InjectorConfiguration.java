package com.github.ruediste.simpledi.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.simpledi.Module;

/**
 * Contains the whole configuration of an injector. Passed to all {@link Module}
 * s of an injector in order to be initialized.
 * 
 */
public class InjectorConfiguration extends AttachedPropertyBearerBase {

	/**
	 * Rules to instantiate dependencies without bindings
	 */
	public final List<InstanceCreationRule> creationRules = new ArrayList<>();

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

	public final List<Dependency<?>> requestedEagerInstantiations = new ArrayList<>();
	public final Map<Object, Object> requestedMemberInjections = new IdentityHashMap<>();

}
