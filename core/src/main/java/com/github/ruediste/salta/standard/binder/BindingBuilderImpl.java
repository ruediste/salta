package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CompiledSupplier;
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
import com.github.ruediste.salta.standard.config.MemberInjectionToken;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public class BindingBuilderImpl<T> implements AnnotatedBindingBuilder<T> {
	protected DependencyKey<T> eagerInstantiationDependency;
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
		eagerInstantiationDependency = DependencyKey.of(type);
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
		return to(DependencyKey.of(implementation));
	}

	@Override
	public ScopedBindingBuilder<T> to(
			CoreDependencyKey<? extends T> implementation) {

		recipeFactorySupplier = () -> ctx -> {
			config.typesBoundToDefaultCreationRecipe.add(implementation
					.getType());
			DefaultCreationRecipeBuilder builder = new DefaultCreationRecipeBuilder(
					config, implementation.getType());
			return builder.build(ctx);
		};
		scopeSupplier = () -> config.getScope(implementation.getType());
		return this;
	}

	@Override
	public void toInstance(T instance) {
		if (instance == null)
			throw new SaltaException(
					"Binding to null instances is not allowed. Use toProvider(Providers.of(null))");
		MemberInjectionToken<T> token = MemberInjectionToken
				.getMemberInjectionToken(injector, instance);
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

	@Override
	public ScopedBindingBuilder<T> toProvider(
			InstanceProvider<? extends T> provider) {
		MemberInjectionToken<InstanceProvider<?>> token = MemberInjectionToken
				.getMemberInjectionToken(injector, provider);
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

	private static class ProviderByKeyInvocationRecipe extends SupplierRecipe {

		InstanceProvider<?> delegate;
		private CoreDependencyKey<?> providerKey;

		public ProviderByKeyInvocationRecipe(CoreDependencyKey<?> providerKey) {
			this.providerKey = providerKey;
		}

		@Override
		protected Class<?> compileImpl(GeneratorAdapter mv,
				MethodCompilationContext ctx) {

			if (delegate == null)
				throw new RecursiveAccessOfInstanceOfProviderClassException(
						providerKey.toString());

			ctx.addFieldAndLoad(InstanceProvider.class, delegate);
			mv.invokeInterface(Type.getType(InstanceProvider.class),
					Method.getMethod("Object get()"));
			return Object.class;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<P> providerKey,
			Function<? super P, InstanceProvider<? extends T>> providerWrapper) {
		scopeSupplier = () -> config.defaultScope;
		recipeFactorySupplier = () -> {
			config.implicitlyBoundKeys.add(providerKey);
			return ctx -> {
				// entered when creating the recipe
				ProviderByKeyInvocationRecipe invocationRecipe = new ProviderByKeyInvocationRecipe(
						providerKey);
				ctx.queueAction(() -> {
					SupplierRecipe recipe = ctx.getRecipe(providerKey);

					CompiledSupplier compiledSupplier = ctx.getCompiler()
							.compileSupplier(recipe);
					invocationRecipe.delegate = providerWrapper
							.apply((P) compiledSupplier.getNoThrow());
				});
				return invocationRecipe;
			};
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
		recipeFactorySupplier = () -> creationContext -> {
			DefaultCreationRecipeBuilder builder = new DefaultCreationRecipeBuilder(
					config, type);
			builder.constructionRecipeSupplier = (ctx) -> config.fixedConstructorInstantiatorFactory
					.create(type, ctx, constructor);
			return builder.build(creationContext);
		};
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
		config.dynamicInitializers.add(injector -> injector
				.getInstance(eagerInstantiationDependency));
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> availableAnnotationType) {
		eagerInstantiationDependency = eagerInstantiationDependency
				.withAnnotations(availableAnnotationType);
		annotationMatcher = config
				.requredQualifierMatcher(availableAnnotationType);
		return this;
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation) {
		eagerInstantiationDependency = eagerInstantiationDependency
				.withAnnotations(availableAnnotation);
		annotationMatcher = config.requredQualifierMatcher(availableAnnotation);
		return this;
	}

}
