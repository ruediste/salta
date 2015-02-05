package com.github.ruediste.simpledi.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.simpledi.standard.Module;
import com.google.common.reflect.TypeToken;

/**
 * Contains the whole configuration of an injector. Passed to all {@link Module}
 * s of an injector in order to be initialized.
 * 
 */
public class InjectorConfiguration extends AttachedPropertyBearerBase {

	public final Stage stage;

	public InjectorConfiguration(Stage stage) {
		this.stage = stage;
	}

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

	/**
	 * Collect error messages to be shown when attempting to create the
	 * injector. Do not add errors while creating the injector.
	 */
	public final List<Message> errorMessages = new ArrayList<>();

	/**
	 * Initializers run once the Injector is constructed. These initializers may
	 * not create request instances from the injector.
	 */
	public final List<Consumer<Injector>> staticInitializers = new ArrayList<>();

	/**
	 * Initializers run once the Injector is constructed. Run after the
	 * {@link #staticInitializers}. These initializers can freely use the
	 * injector
	 */
	public final List<Consumer<Injector>> dynamicInitializers = new ArrayList<>();

	/**
	 * List of dependencies which sould be created after the creation of the
	 * injector
	 */
	public final List<Dependency<?>> requestedEagerInstantiations = new ArrayList<>();

	public boolean disableCircularProxies;

	public boolean requireAtInjectOnConstructors;

	public MemberInjectionStrategy memberInjectionStrategy;

	/**
	 * Create a token to access the value which makes sure that the members of
	 * the value are injected when
	 * {@link MemberInjectionToken#getValue(ContextualInjector)} returns. Only a
	 * single token for a single value (compared by identity) is ever created.
	 */
	@SuppressWarnings("unchecked")
	public <T> MemberInjectionToken<T> getMemberInjectionToken(T value) {
		return getMemberInjectionToken(value,
				(TypeToken<T>) TypeToken.of(value.getClass()));
	}

	/**
	 * Create a token to access the value which makes sure that the members of
	 * the value are injected when
	 * {@link MemberInjectionToken#getValue(ContextualInjector)} returns. Only a
	 * single token for a single value (compared by identity) is ever created.
	 */
	public <T> MemberInjectionToken<T> getMemberInjectionToken(T value,
			TypeToken<T> type) {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			MemberInjectionToken<T> token = (MemberInjectionToken<T>) memberInjectionTokens
					.get(value);

			if (token == null) {
				token = new MemberInjectionToken<T>(value, type);
				memberInjectionTokens.put(value, token);
			}
			return token;
		}
	}

	private final Map<Object, MemberInjectionToken<?>> memberInjectionTokens = new IdentityHashMap<>();

	/**
	 * A token which makes sure that the members of the contained value are
	 * injected before the value is returned.
	 */
	public static class MemberInjectionToken<T> {
		private T value;
		private boolean injected;
		private TypeToken<T> type;

		private MemberInjectionToken(T value, TypeToken<T> type) {
			this.value = value;
			this.type = type;
		}

		public T getValue(ContextualInjector injector) {
			synchronized (this) {
				if (!injected) {
					injector.injectMembers(type, value);
					injected = true;
				}
			}
			return value;
		}
	}
}
