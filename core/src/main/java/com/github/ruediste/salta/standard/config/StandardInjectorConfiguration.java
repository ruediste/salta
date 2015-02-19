package com.github.ruediste.salta.standard.config;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Message;
import com.github.ruediste.salta.standard.Scope;
import com.github.ruediste.salta.standard.ScopeRule;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.binder.AnnotatedBindingBuilder;
import com.github.ruediste.salta.standard.recipe.RecipeInjectionListener;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

public class StandardInjectorConfiguration {
	public CoreInjectorConfiguration config;
	public final Stage stage;

	public StandardInjectorConfiguration(Stage stage,
			CoreInjectorConfiguration config) {
		this.stage = stage;
		this.config = config;
	}

	public Scope singletonScope = new SingletonScope();
	public Scope defaultScope = new DefaultScope();
	public final Map<Class<? extends Annotation>, Scope> scopeAnnotationMap = new HashMap<>();

	/**
	 * Strategy to create a {@link RecipeInstantiator} given a constructor.
	 */
	public BiFunction<Constructor<?>, TypeToken<?>, RecipeInstantiator> fixedConstructorInstantiatorFactory;

	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();

	/**
	 * Rules used to create {@link RecipeMembersInjector}s for a type. The
	 * injectors of the first one returning a non-null result are used.
	 */
	public final List<MembersInjectorRule> membersInjectorRules = new ArrayList<>();

	/**
	 * Default list of factories used to create {@link RecipeMembersInjector}s
	 * for a given type. These factories are used if no member of the
	 * {@link #membersInjectorRules} matches for a type.
	 */
	public final List<RecipeMembersInjectorFactory> defaultMembersInjectorFactories = new ArrayList<>();

	/**
	 * Create a list of members injectors based on the
	 * {@link #membersInjectorRules} and as fallback the
	 * {@link #defaultMembersInjectorFactories}
	 */
	public List<RecipeMembersInjector> createRecipeMembersInjectors(
			RecipeCreationContext ctx, TypeToken<?> type) {
		// test rules
		for (MembersInjectorRule rule : membersInjectorRules) {
			List<RecipeMembersInjector> membersInjectors = rule
					.getMembersInjectors(type);
			if (membersInjectors != null)
				return membersInjectors;
		}

		// use default factories
		ArrayList<RecipeMembersInjector> result = new ArrayList<>();
		for (RecipeMembersInjectorFactory factory : defaultMembersInjectorFactories) {
			result.addAll(factory.createInjectors(ctx, type));
		}
		return result;
	}

	/**
	 * List of rules to listen for injections. The listeners of all rules are
	 * combined.
	 */
	public final List<InjectionListenerRule> injectionListenerRules = new ArrayList<>();

	public List<RecipeInjectionListener> createInjectionListeners(
			RecipeCreationContext ctx, TypeToken<?> type) {
		return injectionListenerRules.stream()
				.map(r -> r.getListener(ctx, type)).filter(x -> x != null)
				.collect(toList());
	}

	/**
	 * List of rules to determine the scope used for a type.
	 */
	public final List<ScopeRule> scopeRules = new ArrayList<>();

	public final Set<Class<?>> requestedStaticInjections = new HashSet<>();

	/**
	 * Create an {@link RecipeInstantiator} using the {@link #instantiatorRules}
	 * 
	 * @param ctx
	 */
	public <T> RecipeInstantiator createRecipeInstantiator(
			RecipeCreationContext ctx, TypeToken<?> type) {
		for (InstantiatorRule rule : instantiatorRules) {
			RecipeInstantiator instantiator = rule.apply(ctx, type);
			if (instantiator != null) {
				return instantiator;
			}
		}
		return null;
	}

	/**
	 * Get the scope for a type based on the {@link #scopeRules} followed by the
	 * {@link #scopeAnnotationMap}, falling back to the {@link #defaultScope}
	 */
	public Scope getScope(TypeToken<?> type) {
		Scope scope = null;

		// evaluate scope rules
		for (ScopeRule rule : scopeRules) {
			scope = rule.getScope(type);
			if (scope != null)
				return scope;
		}

		// scan scope annotation map
		for (Entry<Class<? extends Annotation>, Scope> entry : scopeAnnotationMap
				.entrySet()) {
			if (type.getRawType().isAnnotationPresent(entry.getKey())) {
				if (scope != null)
					throw new ProvisionException(
							"Multiple scope annotations present on " + type);
				scope = entry.getValue();
			}
		}

		if (scope == null)
			scope = defaultScope;
		return scope;
	}

	public Scope getScope(Class<? extends Annotation> scopeAnnotation) {
		Scope scope = scopeAnnotationMap.get(scopeAnnotation);
		if (scope == null)
			throw new ProvisionException("Unknown scope annotation "
					+ scopeAnnotation);
		return scope;
	}

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
	public final List<CoreDependencyKey<?>> requestedEagerInstantiations = new ArrayList<>();

	public boolean requireAtInjectOnConstructors;

	/**
	 * Extractors of the required qualifier. This qualifier will be matched
	 * against the qualifier available on types (JIT bindings, see
	 * {@link #availableQualifierExtractors}) or the available qualifier
	 * specified on bindings (see {@link AnnotatedBindingBuilder}). All
	 * extractors are invoked. If more than one qualifier is found, an error is
	 * raised
	 */
	public final List<Function<CoreDependencyKey<?>, Stream<Annotation>>> requiredQualifierExtractors = new ArrayList<>();

	/**
	 * Use the {@link #requiredQualifierExtractors} to determine the required
	 * qualifier of a key (or null)
	 */
	public Annotation getRequiredQualifier(CoreDependencyKey<?> key) {
		List<Annotation> qualifiers = requiredQualifierExtractors.stream()
				.flatMap(f -> f.apply(key)).collect(toList());
		if (qualifiers.size() > 1)
			throw new ProvisionException(
					"Multiple required qualifiers found on " + key + ": "
							+ qualifiers);
		return Iterables.getOnlyElement(qualifiers, null);
	}

	/**
	 * Extractors for qualifier available on a type. Used by JIT bindings.
	 */
	public final List<Function<Class<?>, Stream<Annotation>>> availableQualifierExtractors = new ArrayList<>();

	/**
	 * Use the {@link #availableQualifierExtractors} to get the available
	 * qualifier of a type. All extractors are invoked. If more than one
	 * qualifier is found, an error is raised
	 */
	public Annotation getAvailableQualifier(Class<?> clazz) {
		List<Annotation> qualifiers = availableQualifierExtractors.stream()
				.flatMap(f -> f.apply(clazz)).collect(toList());
		if (qualifiers.size() > 1)
			throw new ProvisionException(
					"Multiple avalable qualifiers found on " + clazz.getName()
							+ ": " + qualifiers);
		return Iterables.getOnlyElement(qualifiers, null);
	}
}
