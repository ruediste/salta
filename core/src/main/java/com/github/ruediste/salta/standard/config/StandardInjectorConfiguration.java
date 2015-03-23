package com.github.ruediste.salta.standard.config;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.Message;
import com.github.ruediste.salta.standard.ScopeRule;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.StandardAnnotatedBindingBuilder;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.github.ruediste.salta.standard.recipe.RecipeInitializer;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

/**
 * Configuration for the {@link StandardInjector}.
 */
public class StandardInjectorConfiguration {
	public CoreInjectorConfiguration config;
	public final Stage stage;

	public StandardInjectorConfiguration(Stage stage) {
		this(stage, new CoreInjectorConfiguration());
	}

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
	public FixedConstructorInstantiatorFactory fixedConstructorInstantiatorFactory;

	/**
	 * Rules defining how to construct an instance of a type. This can is used
	 * from various places like {@link Binding}s or {@link CreationRule}s.
	 * 
	 * <p>
	 * Not to be confused with the
	 * {@link CoreInjectorConfiguration#creationRules}, which determine how to
	 * create a rule given a {@link CoreDependencyKey}
	 * </p>
	 */
	public final List<ConstructionRule> constructionRules = new ArrayList<>();

	public Optional<Function<RecipeCreationContext, SupplierRecipe>> createConstructionRecipe(
			TypeToken<?> type) {
		for (ConstructionRule rule : constructionRules) {
			Function<RecipeCreationContext, SupplierRecipe> result = rule
					.createConstructionRecipe(type);
			if (result != null)
				return Optional.of(result);
		}
		return Optional.empty();
	}

	/**
	 * List of rules to create an instantiator given a type. The first matching
	 * rule is used
	 */
	public final List<InstantiatorRule> instantiatorRules = new ArrayList<>();

	/**
	 * Create an {@link RecipeInstantiator} function using the
	 * {@link #instantiatorRules} If no instantiator is found,
	 * {@link Optional#empty()} is returned.
	 */
	public Optional<Function<RecipeCreationContext, RecipeInstantiator>> createRecipeInstantiator(
			TypeToken<?> type) {
		for (InstantiatorRule rule : instantiatorRules) {
			Optional<Function<RecipeCreationContext, RecipeInstantiator>> instantiator = rule
					.apply(type);
			if (instantiator.isPresent()) {
				return instantiator;
			}
		}
		return Optional.empty();
	}

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
	 * List of rules enhance instances. The enhancers of all rules are combined.
	 */
	public final List<RecipeInitializerFactory> initializerFactories = new ArrayList<>();

	public List<RecipeInitializer> createInitializers(
			RecipeCreationContext ctx, TypeToken<?> type) {
		return initializerFactories.stream()
				.flatMap(r -> r.getInitializers(ctx, type).stream())
				.filter(x -> x != null).collect(toList());
	}

	/**
	 * List of enhancer factories. The enhancers of all factories are combined.
	 */
	public final List<EnhancerFactory> enhancerFactories = new ArrayList<>();

	public List<RecipeEnhancer> createEnhancers(RecipeCreationContext ctx,
			TypeToken<?> type) {
		return enhancerFactories.stream().map(r -> r.getEnhancer(ctx, type))
				.filter(x -> x != null).collect(toList());
	}

	/**
	 * List of rules to determine the scope used for a type.
	 */
	public final List<ScopeRule> scopeRules = new ArrayList<>();

	public final Set<Class<?>> requestedStaticInjections = new HashSet<>();

	/**
	 * Get the scope for a type based on the {@link #scopeRules} followed by the
	 * {@link #scopeAnnotationMap}, falling back to the {@link #defaultScope}
	 */
	public Scope getScope(TypeToken<?> type) {

		// evaluate scope rules
		for (ScopeRule rule : scopeRules) {
			Scope scope = rule.getScope(type);
			if (scope != null)
				return scope;
		}

		return getScope(type.getRawType());
	}

	public Scope getScope(AnnotatedElement element) {
		Scope scope = null;
		// scan scope annotation map
		for (Entry<Class<? extends Annotation>, Scope> entry : scopeAnnotationMap
				.entrySet()) {
			if (element.isAnnotationPresent(entry.getKey())) {
				if (scope != null)
					throw new SaltaException(
							"Multiple scope annotations present on " + element);
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
			throw new SaltaException("Unknown scope annotation "
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
	public final List<Runnable> staticInitializers = new ArrayList<>();

	/**
	 * Initializers run once the Injector is constructed. Run after the
	 * {@link #staticInitializers}. These initializers can freely use the
	 * injector
	 */
	public final List<Runnable> dynamicInitializers = new ArrayList<>();

	/**
	 * List of dependencies which sould be created after the creation of the
	 * injector
	 */
	public final List<CoreDependencyKey<?>> requestedEagerInstantiations = new ArrayList<>();

	/**
	 * Extractors of the required qualifier. This qualifier will be matched
	 * against the qualifier available on types (JIT bindings, see
	 * {@link #availableQualifierExtractors}) or the available qualifier
	 * specified on bindings (see {@link StandardAnnotatedBindingBuilder}). All
	 * extractors are invoked. If more than one qualifier is found, an error is
	 * raised
	 */
	public final List<Function<AnnotatedElement, Stream<Annotation>>> requiredQualifierExtractors = new ArrayList<>();

	/**
	 * Use the {@link #requiredQualifierExtractors} to determine the required
	 * qualifier of a key (or null)
	 */
	public Annotation getRequiredQualifier(CoreDependencyKey<?> key) {
		return getRequiredQualifier(key, key.getAnnotatedElement());
	}

	public Annotation getRequiredQualifier(Object source,
			AnnotatedElement annotatedElement) {
		List<Annotation> qualifiers = requiredQualifierExtractors.stream()
				.flatMap(f -> f.apply(annotatedElement)).collect(toList());
		if (qualifiers.size() > 1)
			throw new SaltaException("Multiple required qualifiers found on "
					+ source + ": " + qualifiers);
		return Iterables.getOnlyElement(qualifiers, null);
	}

	public Matcher<CoreDependencyKey<?>> requredQualifierMatcher(
			Annotation availableQualifier) {
		return new RequiredQualifierMatcher(this, availableQualifier);
	}

	private static final class RequiredQualifierMatcher implements
			Matcher<CoreDependencyKey<?>> {
		private Annotation availableQualifier;
		private StandardInjectorConfiguration config;

		public RequiredQualifierMatcher(StandardInjectorConfiguration config,
				Annotation availableQualifier) {
			this.config = config;
			this.availableQualifier = availableQualifier;
		}

		@Override
		public boolean matches(CoreDependencyKey<?> key) {
			return config.doQualifiersMatch(config.getRequiredQualifier(key),
					availableQualifier);
		}

		@Override
		public int hashCode() {
			return availableQualifier == null ? 0 : availableQualifier
					.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != getClass())
				return false;
			RequiredQualifierMatcher other = (RequiredQualifierMatcher) obj;
			return Objects.equals(availableQualifier, other.availableQualifier);
		}

		@Override
		public String toString() {
			return "qualifier=" + availableQualifier;
		}
	}

	/**
	 * Return a Matcher which matches if the key has a qualifier of the provided
	 * type
	 */
	public Matcher<CoreDependencyKey<?>> requredQualifierMatcher(
			Class<? extends Annotation> availableQualifierType) {
		return new RequiredQualifierTypeMatcher(this, availableQualifierType);
	}

	private static final class RequiredQualifierTypeMatcher implements
			Matcher<CoreDependencyKey<?>> {
		private StandardInjectorConfiguration config;
		private Class<? extends Annotation> availableQualifierType;

		public RequiredQualifierTypeMatcher(
				StandardInjectorConfiguration config,
				Class<? extends Annotation> availableQualifierType) {
			this.config = config;
			this.availableQualifierType = availableQualifierType;
		}

		@Override
		public boolean matches(CoreDependencyKey<?> key) {
			Annotation requiredQualifier = config.getRequiredQualifier(key);
			return config.doQualifiersMatch(requiredQualifier,
					availableQualifierType);
		}

		@Override
		public int hashCode() {
			return availableQualifierType == null ? 0 : availableQualifierType
					.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != getClass())
				return false;
			RequiredQualifierTypeMatcher other = (RequiredQualifierTypeMatcher) obj;
			return Objects.equals(availableQualifierType,
					other.availableQualifierType);
		}

		@Override
		public String toString() {
			return "qualifier is" + availableQualifierType;
		}
	}

	/**
	 * Extractors for the available qualifier of an annotated elements. Used by
	 * JIT bindings or to determine the qualifiers of provider methods.
	 */
	public final List<Function<AnnotatedElement, Stream<Annotation>>> availableQualifierExtractors = new ArrayList<>();

	/**
	 * Use the {@link #availableQualifierExtractors} to get the available
	 * qualifier of a type. All extractors are invoked. If more than one
	 * qualifier is found, an error is raised
	 */
	public Annotation getAvailableQualifier(AnnotatedElement element) {
		List<Annotation> qualifiers = availableQualifierExtractors.stream()
				.flatMap(f -> f.apply(element)).collect(toList());
		if (qualifiers.size() > 1)
			throw new SaltaException("Multiple avalable qualifiers found on\n"
					+ element + ":\n" + qualifiers);
		return Iterables.getOnlyElement(qualifiers, null);
	}

	/**
	 * Rule to determine if an available annotation (second argument) satisfies
	 * a required annotation (first argument). If no rule matches, the two
	 * annotations are simply compared for equality
	 */
	public final List<BiFunction<Annotation, Annotation, Boolean>> qualifierMatchingAnnotationRules = new ArrayList<>();

	/**
	 * Check if an available qualifier matches the reqired qualifier
	 */
	public boolean doQualifiersMatch(Annotation requiredQualifier,
			Annotation availableQualifer) {
		for (BiFunction<Annotation, Annotation, Boolean> func : qualifierMatchingAnnotationRules) {
			Boolean result = func.apply(requiredQualifier, availableQualifer);
			if (result != null)
				return result;
		}
		return Objects.equals(requiredQualifier, availableQualifer);
	}

	/**
	 * Rule to determine if an available annotation type (second argument)
	 * satisfies a required annotation (first argument). If no rule matches, it
	 * is simply checked if the required annotation has the type of the
	 * available qualifier.
	 */
	public final List<BiFunction<Annotation, Class<?>, Boolean>> qualifierMatchingTypeRules = new ArrayList<>();

	/**
	 * check if an available qualifier type matches a required qualifier
	 */
	public boolean doQualifiersMatch(Annotation requiredQualifier,
			Class<?> availableQualiferType) {
		for (BiFunction<Annotation, Class<?>, Boolean> func : qualifierMatchingTypeRules) {
			Boolean result = func.apply(requiredQualifier,
					availableQualiferType);
			if (result != null)
				return result;
		}

		if (requiredQualifier == null)
			return availableQualiferType == null;
		else
			return requiredQualifier.annotationType().equals(
					availableQualiferType);
	}

	public final ArrayList<TypeToken<?>> typesBoundToDefaultCreationRecipe = new ArrayList<>();
	public final ArrayList<CoreDependencyKey<?>> implicitlyBoundKeys = new ArrayList<>();

	public MembersInjectorFactory membersInjectorFactory;
}
