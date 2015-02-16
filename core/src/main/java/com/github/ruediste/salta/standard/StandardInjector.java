package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledParameterizedCreationRecipe;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.google.common.reflect.TypeToken;

public class StandardInjector implements Injector {

	private final static class ClassDependencyKey<T> extends
			CoreDependencyKey<T> {

		private Class<T> type;

		ClassDependencyKey(Class<T> type) {
			this.type = type;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof ClassDependencyKey) {
				return type.equals(((ClassDependencyKey<?>) obj).type);
			}
			return false;

		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public TypeToken<T> getType() {
			return TypeToken.of(type);
		}

		@Override
		public Class<T> getRawType() {
			return type;
		}

		@Override
		public AnnotatedElement getAnnotatedElement() {
			return new AnnotatedElement() {

				@Override
				public Annotation[] getDeclaredAnnotations() {
					return new Annotation[] {};
				}

				@Override
				public Annotation[] getAnnotations() {
					return getDeclaredAnnotations();
				}

				@SuppressWarnings("unchecked")
				@Override
				public <A extends Annotation> A getAnnotation(
						Class<A> annotationClass) {
					return null;
				}
			};
		}
	}

	private final class ProviderImpl<T> implements Provider<T> {

		private Supplier<T> factoryFunction;

		public ProviderImpl(CoreDependencyKey<T> key) {
			factoryFunction = coreInjector.getInstanceSupplier(key);
		}

		@Override
		public T get() {
			checkInitialized();
			return factoryFunction.get();
		}
	}

	private final class MembersInjectorImpl<T> implements MembersInjector<T> {
		private CompiledParameterizedCreationRecipe compiledRecipe;
		private TypeToken<T> type;

		public MembersInjectorImpl(TypeToken<T> type) {
			this.type = type;
			synchronized (coreInjector.recipeLock) {
				RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
						coreInjector);
				List<RecipeMembersInjector> injectors = config
						.createRecipeMembersInjectors(ctx, type);
				ctx.processQueuedActions();
				CreationRecipe recipe = new CreationRecipe() {

					@Override
					public void compile(GeneratorAdapter mv,
							RecipeCompilationContext compilationContext) {
						mv.loadArg(0);

						for (RecipeMembersInjector rmi : injectors) {
							rmi.compile(mv, compilationContext);
						}
					}
				};
				compiledRecipe = coreInjector
						.compileParameterizedRecipe(recipe);
			}
		}

		@Override
		public void injectMembers(T instance) {
			checkInitialized();
			try {
				compiledRecipe.get(instance);
			} catch (ProvisionException e) {
				throw e;
			} catch (Throwable e) {
				throw new ProvisionException(
						"Error while injecting members of instance of " + type,
						e);
			}
		}
	}

	private boolean initialized;
	private StandardInjectorConfiguration config;
	private CoreInjector coreInjector;

	private void checkInitialized() {
		if (!initialized) {
			throw new ProvisionException(
					"Cannot use injector before it is initialized");
		}
	}

	public void initialize(StandardInjectorConfiguration config) {
		this.config = config;
		coreInjector = new CoreInjector(config.config);
		for (Consumer<Injector> initializer : config.staticInitializers) {
			initializer.accept(this);
		}
		initialized = true;
		for (Consumer<Injector> initializer : config.dynamicInitializers) {
			initializer.accept(this);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void injectMembers(Object instance) {
		injectMembers((TypeToken) TypeToken.of(instance.getClass()), instance);
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		checkInitialized();
		getMembersInjector(type).injectMembers(instance);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeToken<T> typeLiteral) {
		return new MembersInjectorImpl<T>(typeLiteral);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return getMembersInjector(TypeToken.of(type));
	}

	@Override
	public <T> Provider<T> getProvider(CoreDependencyKey<T> key) {

		return new ProviderImpl<T>(key);
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		return getProvider(DependencyKey.of(type));
	}

	@Override
	public <T> T getInstance(CoreDependencyKey<T> key) {

		checkInitialized();
		return coreInjector.getInstance(key);
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		checkInitialized();
		return coreInjector.getInstance(new ClassDependencyKey<T>(type));
	}
}
