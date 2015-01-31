package com.github.ruediste.simpledi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public class InstantiationRecipe {

	public Supplier<?> instantiator;
	public Scope scope;

	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public final List<MembersInjector<?>> membersInjectors = new ArrayList<>();
	public final List<InjectionListener<?>> injectionListeners = new ArrayList<>();

	private boolean isInjectionPointSpecific;

	public boolean isInjectionPointSpecific() {
		return isInjectionPointSpecific;
	}

	public void makeInjectionPointSpecific() {
		this.isInjectionPointSpecific = true;
	}

}
