package com.github.ruediste.salta.guice;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.jsr330.JSR330ConstructorInstantiatorRule;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.ProviderMethodBinder;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.InstantiatorRule;
import com.github.ruediste.salta.standard.config.SingletonScope;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ImplementedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.MembersInjectorCreationRuleBase;
import com.github.ruediste.salta.standard.util.ProvidedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.ProviderDependencyFactoryRule;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

public class GuiceModule extends AbstractModule {

	private GuiceInjectorConfiguration guiceConfig;
	private GuiceInjectorImpl injector;

	public GuiceModule(GuiceInjectorConfiguration guiceConfig,
			GuiceInjectorImpl injector) {
		this.guiceConfig = guiceConfig;
		this.injector = injector;
	}

	@Override
	protected void configure() {

		StandardInjectorConfiguration config = binder().getConfiguration();

		// make Named annotations of javax.inject and Guice equivalent
		config.qualifierMatchingAnnotationRules
				.add(new BiFunction<Annotation, Annotation, Boolean>() {
					@Override
					public Boolean apply(Annotation required,
							Annotation available) {
						if (required instanceof javax.inject.Named
								&& available instanceof Named) {
							return Objects.equal(
									((javax.inject.Named) required).value(),
									((Named) available).value());
						}
						if (available instanceof javax.inject.Named
								&& required instanceof Named) {
							return Objects.equal(((Named) required).value(),
									((javax.inject.Named) available).value());
						}
						return null;
					}
				});
		config.qualifierMatchingTypeRules
				.add(new BiFunction<Annotation, Class<?>, Boolean>() {

					@Override
					public Boolean apply(Annotation required,
							Class<?> availableType) {
						if (Named.class.equals(availableType)
								|| javax.inject.Named.class
										.equals(availableType)) {
							return Named.class.equals(required.annotationType())
									|| javax.inject.Named.class.equals(required
											.annotationType());
						}
						return null;
					}
				});

		config.config.creationRules.add(new CreationRule() {

			@Override
			public SupplierRecipe apply(CoreDependencyKey<?> key,
					RecipeCreationContext ctx) {
				if (Stage.class.equals(key.getType().getType()))
					return new SupplierRecipeImpl(() -> guiceConfig.stage);
				else
					return null;
			}
		});

		config.defaultMembersInjectorFactories
				.add(new GuiceMembersInjectorFactory(config));

		// provider creation rule
		config.config.creationRules
				.add(new ProviderDependencyFactoryRule(key -> key.getRawType()
						.equals(Provider.class),
						(type, supplier) -> (Provider<?>) supplier::get,
						Provider.class));

		// members injector creation rule
		config.config.creationRules.add(new MembersInjectorCreationRuleBase(
				config) {
			@Override
			protected TypeToken<?> getDependency(CoreDependencyKey<?> key) {
				if (!MembersInjector.class.equals(key.getRawType()))
					return null;

				if (key.getType().getType() instanceof Class) {
					throw new SaltaException(
							"Cannot inject a MembersInjector that has no type parameter");
				}
				TypeToken<?> dependency = key.getType().resolveType(
						MembersInjector.class.getTypeParameters()[0]);
				return dependency;
			}

			@Override
			protected Object wrapInjector(Consumer<Object> saltaMembersInjector) {
				Object wrappedInjector = new MembersInjector<Object>() {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public void injectMembers(Object x) {
						saltaMembersInjector.accept(x);
					}

					@Override
					public String toString() {
						return saltaMembersInjector.toString();
					};
				};
				return wrappedInjector;
			}

			@Override
			protected Class<?> getWrappedInjectorType() {
				return MembersInjector.class;
			}
		});

		config.requiredQualifierExtractors.add(annotatedElement -> {
			return Arrays.stream(annotatedElement.getAnnotations()).filter(
					a -> a.annotationType().isAnnotationPresent(
							BindingAnnotation.class));
		});

		config.availableQualifierExtractors
				.add(new Function<AnnotatedElement, Stream<Annotation>>() {
					@Override
					public Stream<Annotation> apply(AnnotatedElement annotated) {
						return Arrays.stream(annotated.getAnnotations())
								.filter(a -> a.annotationType()
										.isAnnotationPresent(
												BindingAnnotation.class));
					}
				});

		// Rule for type literals
		config.config.creationRules
				.add(new CreationRuleImpl(
						k -> TypeLiteral.class.equals(k.getRawType()),
						key -> {
							TypeToken<?> type = key.getType();
							if (type.getType() instanceof Class) {
								throw new SaltaException(
										"Cannot inject a TypeLiteral that has no type parameter");
							}
							TypeToken<?> typeParameter = type
									.resolveType(TypeLiteral.class
											.getTypeParameters()[0]);
							if (typeParameter.getType() instanceof TypeVariable)
								throw new SaltaException(
										"TypeLiteral<"
												+ typeParameter
												+ "> cannot be used as a key; It is not fully specified.");
							TypeLiteral<?> typeLiteral = TypeLiteral
									.get(typeParameter.getType());
							return () -> typeLiteral;
						}));

		bind(Injector.class).toInstance(injector);

		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new GuiceStaticMemberInjector()
				.injectStaticMembers(config, i));

		// scan modules for @Provides methods
		{
			ProviderMethodBinder b = new ProviderMethodBinder(config) {

				@Override
				protected boolean isProviderMethod(Method m) {
					if (!m.isAnnotationPresent(Provides.class))
						return false;
					if (void.class.equals(m.getReturnType())) {
						throw new SaltaException(
								"@Provides method may not return void");
					}
					return true;
				}
			};
			config.modulePostProcessors.add(b::bindProviderMethodsOf);
		}
		bindScope(Singleton.class, config.singletonScope);

		config.staticInitializers.add(injector::setDelegate);

		// add rule for ProvidedBy and ImplementedBy
		for (TypeToken<?> type : config.typesBoundToDefaultCreationRecipe) {
			ProvidedBy providedBy = type.getRawType().getAnnotation(
					ProvidedBy.class);
			if (providedBy != null) {
				StandardStaticBinding binding = new StandardStaticBinding();
				binding.dependencyMatcher = DependencyKey
						.rawTypeMatcher(providedBy.value());
				binding.recipeFactory = ctx -> new DefaultCreationRecipeBuilder(
						config, TypeToken.of(providedBy.value())).build(ctx);
				binding.scopeSupplier = () -> config.getScope(providedBy
						.value());
				config.config.automaticStaticBindings.add(binding);
			}
		}
		config.constructionRules.add(new ProvidedByConstructionRuleBase(
				Provider.class) {

			@Override
			protected DependencyKey<?> getProviderKey(TypeToken<?> type) {
				ProvidedBy providedBy = type.getRawType().getAnnotation(
						ProvidedBy.class);
				if (providedBy != null) {
					if (!type.isAssignableFrom(TypeToken.of(providedBy.value())
							.resolveType(Provider.class.getTypeParameters()[0]))) {
						throw new SaltaException("Provider "
								+ providedBy.value()
								+ " specified by @ProvidedBy does not provide "
								+ type);
					}
					return DependencyKey.of(providedBy.value());
				}
				return null;
			}
		});

		if (guiceConfig.requireExplicitBindings) {
			HashSet<Class<?>> rawTypes = new HashSet<Class<?>>();
			for (TypeToken<?> type : config.typesBoundToDefaultCreationRecipe) {
				ImplementedBy implementedBy = type.getRawType().getAnnotation(
						ImplementedBy.class);
				if (implementedBy != null) {
					if (rawTypes.add(implementedBy.value())) {
						StandardStaticBinding binding = new StandardStaticBinding();
						binding.dependencyMatcher = DependencyKey
								.rawTypeMatcher(implementedBy.value());
						binding.recipeFactory = ctx -> new DefaultCreationRecipeBuilder(
								config, TypeToken.of(implementedBy.value()))
								.build(ctx);
						binding.scopeSupplier = () -> config
								.getScope(implementedBy.value());
						config.config.automaticStaticBindings.add(binding);
					}
				}
			}

			HashSet<CoreDependencyKey<?>> keys = new HashSet<>();
			for (CoreDependencyKey<?> foo : config.implicitlyBoundKeys) {
				if (keys.add(foo)) {
					StandardStaticBinding binding = new StandardStaticBinding();
					binding.dependencyMatcher = DependencyKey.matcher(foo);
					binding.recipeFactory = ctx -> new DefaultCreationRecipeBuilder(
							config, foo.getType()).build(ctx);
					binding.scopeSupplier = () -> config
							.getScope(foo.getType());
					config.config.automaticStaticBindings.add(binding);
				}
			}
		}

		config.constructionRules.add(new ImplementedByConstructionRuleBase() {

			@Override
			protected DependencyKey<?> getImplementorKey(TypeToken<?> type) {
				ImplementedBy implementedBy = type.getRawType().getAnnotation(
						ImplementedBy.class);
				if (implementedBy != null) {

					if (type.getRawType().equals(implementedBy.value())) {
						throw new SaltaException(
								"@ImplementedBy points to the same class it annotates. type: "
										+ type);
					}
					return DependencyKey.of(implementedBy.value());
				}
				return null;
			}
		});
		{
			install(new JSR330Module());
			List<InstantiatorRule> tmp = new ArrayList<>(
					config.instantiatorRules);
			config.instantiatorRules.clear();
			config.instantiatorRules
					.addAll(tmp
							.stream()
							.filter(x -> !(x instanceof JSR330ConstructorInstantiatorRule))
							.collect(toList()));
		}

		config.instantiatorRules.add(new GuiceConstructorInstantiatorRule(
				config, guiceConfig.requireAtInjectOnConstructors));

		if (guiceConfig.requireExplicitBindings)
			config.config.jitBindingRules.clear();

		config.dynamicInitializers
				.add(injector -> {
					if (guiceConfig.stage == Stage.PRODUCTION) {
						for (Binding b : config.config.staticBindings) {
							if (b.getScope() instanceof SingletonScope) {
								RecipeCreationContext ctx = new RecipeCreationContextImpl(
										injector.getCoreInjector());
								((SingletonScope) b.getScope()).instantiate(
										ctx, b, ctx.getOrCreateRecipe(b));
							}
						}
					}
				});
	}

}
