package com.github.ruediste.simpledi;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.google.common.reflect.TypeToken;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public class CreationRecipe extends AttachedPropertyBearerBase {

	public Instantiator<?> instantiator;
	public Scope scope;

	/**
	 * Define the constructor to be used.
	 */
	public Constructor<?> constructor;

	/**
	 * Type token to be used to resolve the constructor arguments.
	 */
	public TypeToken<?> constructorTypeToken;

	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public final List<MembersInjector<?>> membersInjectors = new ArrayList<>();
	public final List<InjectionListener<?>> injectionListeners = new ArrayList<>();

}
