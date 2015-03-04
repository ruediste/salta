package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledFunction;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
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

				@Override
				public <A extends Annotation> A getAnnotation(
						Class<A> annotationClass) {
					return null;
				}
			};
		}
	}

	private final class ProviderImpl<T> implements Provider<T> {

		volatile private Supplier<T> supplier;
		private CoreDependencyKey<T> key;

		public ProviderImpl(CoreDependencyKey<T> key) {
			this.key = key;
		}

		@Override
		public T get() {
			checkInitialized();
			if (supplier == null) {
				synchronized (coreInjector.recipeLock) {
					if (supplier == null)
						supplier = coreInjector.getInstanceSupplier(key);
				}
			}
			return supplier.get();
		}

		@Override
		public String toString() {
			return "Provider<" + key + ">";
		}
	}

	private final class MembersInjectorImpl<T> implements MembersInjector<T> {
		private CompiledFunction compiledRecipe;
		private TypeToken<T> type;

		public MembersInjectorImpl(TypeToken<T> type) {
			this.type = type;
			synchronized (coreInjector.recipeLock) {
				RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
						coreInjector);
				List<RecipeMembersInjector> injectors = config
						.createRecipeMembersInjectors(ctx, type);
				ctx.processQueuedActions();
				FunctionRecipe recipe = new FunctionRecipe() {

					@Override
					protected Class<?> compileImpl(Class<?> argumentType,
							GeneratorAdapter mv, MethodCompilationContext ctx) {
						for (RecipeMembersInjector rmi : injectors) {
							argumentType = rmi.compile(argumentType, ctx);
						}
						return argumentType;
					}
				};
				compiledRecipe = coreInjector.compileFunction(recipe);
			}
		}

		@Override
		public void injectMembers(T instance) {
			checkInitialized();
			try {
				compiledRecipe.get(instance);
			} catch (SaltaException e) {
				throw e;
			} catch (Throwable e) {
				throw new SaltaException(
						"Error while injecting members of instance of " + type
								+ "\n" + e.getMessage(), e);
			}
		}
	}

	private boolean initialized;
	private StandardInjectorConfiguration config;
	private CoreInjector coreInjector;

	private void checkInitialized() {
		if (!initialized) {
			throw new SaltaException(
					"Cannot use injector before it is initialized");
		}
	}

	public void initialize(StandardInjectorConfiguration config) {
		this.config = config;

		config.postProcessModules();

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
