package com.github.ruediste.simpledi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Contains the whole configuration of an injector. Passed to all {@link Module}
 * s of an injector in order to be initialized.
 * 
 */
public class InjectorConfiguration extends AttachedPropertyBearerBase {

	public final List<InstanceCreationRule> creationRules = new ArrayList<>();
	public final List<JITBindingRule> jitBindingRules = new ArrayList<>();
	public final List<Binding> bindings = new ArrayList<>();
	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();

	@Deprecated
	public final List<Rule> rules = new ArrayList<>();

	public final List<InstanceRequestEnricher> keyEnrichers = new ArrayList<>();

	@Deprecated
	public void addRule(Rule rule) {
		rules.add(rule);
	}

	public void addKeyEnricher(InstanceRequestEnricher enricher) {
		keyEnrichers.add(enricher);
	}

	public Scope singletonScope;
	public Scope defaultScope;

	public final List<Dependency<?>> requestedEagerInstantiations = new ArrayList<>();
	public final Map<Class<? extends Annotation>, Scope> scopeAnnotationMap = new HashMap<>();

	public final Map<Object, Object> requestedMemberInjections = new IdentityHashMap<>();
}
