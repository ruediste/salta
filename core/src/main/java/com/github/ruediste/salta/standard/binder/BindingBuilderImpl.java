package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.CreationRecipeFactory;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.DefaultConstructionRule;
import com.github.ruediste.salta.standard.config.MembersInjectionToken;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public class BindingBuilderImpl<T> implements AnnotatedBindingBuilder<T> {
	protected Matcher<CoreDependencyKey<?>> typeMatcher;
	protected Matcher<CoreDependencyKey<?>> annotationMatcher;

	private StandardInjectorConfiguration config;
	private TypeToken<T> type;
	/**
	 * Binding which will be constructed. All instance variables will be
	 * overwritten in {@link #register()}
	 */
	private StandardStaticBinding binding;
	private Supplier<CreationRecipeFactory> recipeFactorySupplier;
	private Supplier<Scope> scopeSupplier;
	private Injector injector;

	public BindingBuilderImpl(Matcher<CoreDependencyKey<?>> typeMatcher,
			TypeToken<T> type, StandardInjectorConfiguration config,
			Injector injector) {
		this.injector = injector;
		binding = new StandardStaticBinding();

		this.typeMatcher = typeMatcher;
		this.type = type;
		this.config = config;
		recipeFactorySupplier = () -> {
			config.typesBoundToDefaultCreationRecipe.add(type);
			return ctx -> new DefaultCreationRecipeBuilder(config, type)
					.build(ctx);
		};
		scopeSupplier = () -> config.getScope(type);
	}

	public void register() {
		if (annotationMatcher != null)
			binding.dependencyMatcher = typeMatcher.and(annotationMatcher);
		else
			binding.dependencyMatcher = typeMatcher.and(config
					.requredQualifierMatcher((Annotation) null));

		binding.possibleTypes.add(type);
		binding.recipeFactory = recipeFactorySupplier.get();
		binding.scopeSupplier = scopeSupplier;

		config.config.staticBindings.add(binding);
	}

	@Override
	public ScopedBindingBuilder<T> to(Class<? extends T> implementation) {
		return to(TypeToken.of(implementation));
	}

	@Override
	public ScopedBindingBuilder<T> to(TypeToken<? extends T> implementation) {
		recipeFactorySupplier = () -> ctx -> {
			config.typesBoundToDefaultCreationRecipe.add(implementation);
			DefaultCreationRecipeBuilder builder = new DefaultCreationRecipeBuilder(
					config, implementation);
			return builder.build(ctx);
		};
		scopeSupplier = () -> config.getScope(implementation);
		return this;
	}

	@Override
	public ScopedBindingBuilder<T> to(
			CoreDependencyKey<? extends T> implementation) {

		recipeFactorySupplier = () -> ctx -> {
			return ctx.getRecipe(implementation);
		};
		scopeSupplier = () -> config.defaultScope;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void toInstance(T instance) {
		if (instance == null)
			throw new SaltaException(
					"Binding to null instances is not allowed. Use toProvider(Providers.of(null))");
		MembersInjectionToken<T> token = injector.getMembersInjectionToken(
				instance, (TypeToken<T>) TypeToken.of(instance.getClass()));
		config.dynamicInitializers.add(i -> token.getValue());
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = () -> new CreationRecipeFactory() {
			boolean recipeCreationInProgress;

			@Override
			public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
				if (recipeCreationInProgress) {
					throw new SaltaException("Recipe creation in progress");
				}
				recipeCreationInProgress = true;
				T injected;
				try {
					injected = token.getValue();
				} finally {
					recipeCreationInProgress = false;
				}

				return new SupplierRecipe() {
					@Override
					protected Class<?> compileImpl(GeneratorAdapter mv,
							MethodCompilationContext ctx) {
						ctx.addFieldAndLoad(Object.class, injected);
						return Object.class;
					}
				};
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public ScopedBindingBuilder<T> toProvider(
			InstanceProvider<? extends T> provider) {
		MembersInjectionToken<InstanceProvider<?>> token = injector
				.getMembersInjectionToken(provider,
						(TypeToken<InstanceProvider<?>>) TypeToken.of(provider
								.getClass()));
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = () -> ctx -> new SupplierRecipeImpl(() -> token
				.getValue().get());
		return this;
	}

	@Override
	public ScopedBindingBuilder<T> toProvider(
			Class<? extends InstanceProvider<? extends T>> providerType) {
		return toProvider(TypeToken.of(providerType));
	}

	@Override
	public ScopedBindingBuilder<T> toProvider(
			TypeToken<? extends InstanceProvider<? extends T>> providerType) {
		return toProvider(DependencyKey.of(providerType));
	}

	@Override
	public ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<? extends InstanceProvider<? extends T>> providerKey) {
		return toProvider(providerKey, x -> x);
	}

	public static class RecursiveAccessOfInstanceOfProviderClassException
			extends SaltaException {
		private static final long serialVersionUID = 1L;

		public RecursiveAccessOfInstanceOfProviderClassException(String provider) {
			super(
					"Access of provider before creation finished. Circular dependency of provider "
							+ provider);
		}
	}

	@Override
	public <P> ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<P> providerKey,
			Function<? super P, InstanceProvider<? extends T>> providerWrapper) {
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = new Supplier<CreationRecipeFactory>() {
			@Override
			public CreationRecipeFactory get() {
				config.implicitlyBoundKeys.add(providerKey);
				return new CreationRecipeFactory() {
					@Override
					public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
						// entered when creating the recipe
						SupplierRecipe recipe = ctx.getRecipe(providerKey);

						return new SupplierRecipe() {

							@Override
							protected Class<?> compileImpl(GeneratorAdapter mv,
									MethodCompilationContext ctx) {
								ctx.addFieldAndLoad(Function.class,
										providerWrapper);
								recipe.compile(ctx);
								mv.invokeInterface(
										Type.getType(Function.class),
										Method.getMethod("Object apply(Object)"));
								mv.invokeInterface(
										Type.getType(InstanceProvider.class),
										Method.getMethod("Object get()"));
								return Object.class;
							}
						};
					}
				};
			}
		};
		return this;
	}

	@Override
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor) {
		return toConstructor(constructor,
				TypeToken.of(constructor.getDeclaringClass()));
	}

	@Override
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type) {
		DefaultConstructionRule rule = new DefaultConstructionRule(config) {
			@Override
			protected Optional<Function<RecipeCreationContext, RecipeInstantiator>> createInstantiationRecipe(
					TypeToken<?> type) {
				return Optional
						.of(ctx -> config.fixedConstructorInstantiatorFactory
								.create(type, ctx, constructor));
			}
		};
		recipeFactorySupplier = () -> creationContext -> {
			Function<RecipeCreationContext, SupplierRecipe> seedRecipe = rule
					.createConstructionRecipe(type);
			List<RecipeEnhancer> enhancers = config.createEnhancers(
					creationContext, type);
			return DefaultCreationRecipeBuilder.applyEnhancers(
					seedRecipe.apply(creationContext), enhancers);
		};
		scopeSupplier = () -> config.getScope(constructor.getDeclaringClass());
		return this;
	}

	@Override
	public void in(Class<? extends Annotation> scopeAnnotation) {
		scopeSupplier = () -> config.getScope(scopeAnnotation);
	}

	@Override
	public void in(Scope scope) {
		this.scopeSupplier = () -> scope;

	}

	@Override
	public void asEagerSingleton() {
		this.scopeSupplier = () -> config.singletonScope;
		config.dynamicInitializers.add(injector -> injector.getCoreInjector()
				.withRecipeCreationContext(ctx -> {
					binding.getScope().performEagerInstantiation(ctx, binding);
					return null;
				}));
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> availableAnnotationType) {
		annotationMatcher = config
				.requredQualifierMatcher(availableAnnotationType);
		return this;
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation) {
		annotationMatcher = config.requredQualifierMatcher(availableAnnotation);
		return this;
	}

	@Override
	public String toString() {
		return "BindingBuilder<" + type + ">";
	}
}
