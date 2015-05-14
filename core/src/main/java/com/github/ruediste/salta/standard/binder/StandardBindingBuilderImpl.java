package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.MembersInjectionToken;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public class StandardBindingBuilderImpl<T> implements
		StandardAnnotatedBindingBuilder<T> {
	protected Matcher<CoreDependencyKey<?>> typeMatcher;
	protected Matcher<CoreDependencyKey<?>> annotationMatcher;

	protected StandardInjectorConfiguration config;
	protected TypeToken<T> type;
	/**
	 * Binding which will be constructed. All instance variables will be
	 * overwritten in {@link #register()}
	 */
	protected StandardStaticBinding binding;
	protected Supplier<CreationRecipeFactory> recipeFactorySupplier;
	protected Supplier<Scope> scopeSupplier;
	protected StandardInjector injector;

	public StandardBindingBuilderImpl(
			Matcher<CoreDependencyKey<?>> typeMatcher, TypeToken<T> type,
			StandardInjectorConfiguration config, StandardInjector injector) {
		this.injector = injector;
		binding = new StandardStaticBinding();

		this.typeMatcher = typeMatcher;
		this.type = type;
		this.config = config;
		recipeFactorySupplier = createDefaultCreationRecipeFactorySupplier(type);
		scopeSupplier = () -> config.scope.getScope(type);
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

		config.creationPipeline.staticBindings.add(binding);
	}

	@Override
	public StandardScopedBindingBuilder<T> to(Class<? extends T> implementation) {
		return to(TypeToken.of(implementation));
	}

	@Override
	public StandardScopedBindingBuilder<T> to(
			TypeToken<? extends T> implementation) {
		recipeFactorySupplier = createDefaultCreationRecipeFactorySupplier(implementation);
		scopeSupplier = () -> config.scope.getScope(implementation);
		return this;
	}

	protected Supplier<CreationRecipeFactory> createDefaultCreationRecipeFactorySupplier(
			TypeToken<? extends T> implementation) {
		return () -> {
			return ctx -> config.construction.createConcreteConstructionRecipe(
					implementation, ctx);
		};
	}

	@Override
	public StandardScopedBindingBuilder<T> to(
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
		config.dynamicInitializers.add(() -> token.getValue());
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
	public StandardScopedBindingBuilder<T> toProvider(
			Supplier<? extends T> provider) {
		MembersInjectionToken<Supplier<?>> token = injector
				.getMembersInjectionToken(provider,
						(TypeToken<Supplier<?>>) TypeToken.of(provider
								.getClass()));
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = () -> ctx -> new SupplierRecipeImpl(() -> token
				.getValue().get());
		return this;
	}

	@Override
	public <P> StandardScopedBindingBuilder<T> toProviderInstance(P provider,
			Function<P, Supplier<? extends T>> providerWrapper) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		MembersInjectionToken<P> token = injector.getMembersInjectionToken(
				provider, (TypeToken) TypeToken.of(provider.getClass()));

		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = () -> ctx -> {
			Supplier<? extends T> wrappedProvider = providerWrapper.apply(token
					.getValue());
			return new SupplierRecipeImpl(() -> wrappedProvider.get());
		};
		return this;
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
	public <P> StandardScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<P> providerKey,
			Function<? super P, ? extends T> providerWrapper) {
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = createProviderRecipeFactorySupplier(
				providerKey, providerWrapper);
		return this;
	}

	protected <P> Supplier<CreationRecipeFactory> createProviderRecipeFactorySupplier(
			CoreDependencyKey<P> providerKey,
			Function<? super P, ? extends T> providerWrapper) {
		return new Supplier<CreationRecipeFactory>() {
			@Override
			public CreationRecipeFactory get() {
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
								return Object.class;
							}
						};
					}
				};
			}
		};
	}

	@Override
	public <S extends T> StandardScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor) {
		return toConstructor(constructor,
				TypeToken.of(constructor.getDeclaringClass()));
	}

	@Override
	public <S extends T> StandardScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type) {

		recipeFactorySupplier = () -> ctx -> {
			RecipeInstantiator instantiator = config
					.createFixedConstructorInstantiator(type, ctx, constructor);
			return config.construction.createConstructionRecipe(ctx, type,
					instantiator);

		};
		scopeSupplier = () -> config.scope.getScope(constructor
				.getDeclaringClass());
		return this;
	}

	@Override
	public void in(Class<? extends Annotation> scopeAnnotation) {
		scopeSupplier = () -> config.scope.getScope(scopeAnnotation);
	}

	@Override
	public void in(Scope scope) {
		this.scopeSupplier = () -> scope;

	}

	@Override
	public void asEagerSingleton() {
		this.scopeSupplier = () -> config.singletonScope;
		config.dynamicInitializers.add(() -> injector.getCoreInjector()
				.withRecipeCreationContext(ctx -> {
					binding.getScope().performEagerInstantiation(ctx, binding);
					return null;
				}));
	}

	@Override
	public StandardLinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> availableAnnotationType) {
		annotationMatcher = config
				.requredQualifierMatcher(availableAnnotationType);
		return this;
	}

	@Override
	public StandardLinkedBindingBuilder<T> annotatedWith(
			Annotation availableAnnotation) {
		annotationMatcher = config.requredQualifierMatcher(availableAnnotation);
		return this;
	}

	@Override
	public String toString() {
		return "BindingBuilder<" + type + ">";
	}
}
