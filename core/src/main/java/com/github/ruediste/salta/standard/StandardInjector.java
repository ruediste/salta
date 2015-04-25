package com.github.ruediste.salta.standard;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.config.MembersInjectionToken;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

public class StandardInjector {

	private final static class ClassDependencyKey<T> extends
			CoreDependencyKey<T> {

		private Class<T> type;

		ClassDependencyKey(Class<T> type) {
			Preconditions.checkNotNull(type);
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

	private final class ProviderImpl<T> implements Supplier<T> {

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

	private boolean initialized;
	private StandardInjectorConfiguration config;
	private CoreInjector coreInjector;
	private final Map<Object, MembersInjectionToken<?>> memberInjectionTokens = new IdentityHashMap<>();

	public StandardInjector(StandardInjectorConfiguration config) {
		this.config = config;
	}

	private void checkInitialized() {
		if (!initialized) {
			throw new SaltaException(
					"Cannot use injector before it is initialized");
		}
	}

	public void initialize() {

		if (!config.errorMessages.isEmpty()) {
			throw new SaltaException("There were Errors:\n"
					+ config.errorMessages.stream()
							.map(msg -> msg.getMessage())
							.collect(joining("\n")));
		}

		coreInjector = new CoreInjector(config.config,
				config.creationPipeline.coreCreationRuleSuppliers.stream()
						.map(s -> s.get()).collect(toList()));

		while (!config.staticInitializers.isEmpty()) {
			ArrayList<Runnable> tmp = new ArrayList<>(config.staticInitializers);
			config.staticInitializers.clear();
			for (Runnable initializer : tmp) {
				initializer.run();
			}
		}

		initialized = true;
		while (!config.dynamicInitializers.isEmpty()) {
			ArrayList<Runnable> tmp = new ArrayList<>(
					config.dynamicInitializers);
			config.dynamicInitializers.clear();
			for (Runnable initializer : tmp) {
				initializer.run();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void injectMembers(Object instance) {
		injectMembers((TypeToken) TypeToken.of(instance.getClass()), instance);
	}

	public <T> void injectMembers(TypeToken<T> type, T instance) {
		checkInitialized();
		getMembersInjector(type).accept(instance);
	}

	public <T> Consumer<T> getMembersInjector(TypeToken<T> typeLiteral) {
		return config.membersInjectorFactory.createMembersInjector(typeLiteral);
	}

	public <T> Supplier<T> getProvider(CoreDependencyKey<T> key) {

		return new ProviderImpl<T>(key);
	}

	public <T> Supplier<T> getProvider(Class<T> type) {
		return getProvider(DependencyKey.of(type));
	}

	public <T> T getInstance(CoreDependencyKey<T> key) {

		checkInitialized();
		return coreInjector.getInstance(key);
	}

	public <T> T getInstance(Class<T> type) {
		checkInitialized();
		return coreInjector.getInstance(new ClassDependencyKey<T>(type));
	}

	public CoreInjector getCoreInjector() {
		checkInitialized();
		return coreInjector;
	}

	@SuppressWarnings("unchecked")
	public <T> MembersInjectionToken<T> getMembersInjectionToken(T value) {
		return getMembersInjectionToken(value,
				(TypeToken<T>) TypeToken.of(value.getClass()));
	}

	public <T> MembersInjectionToken<T> getMembersInjectionToken(T value,
			TypeToken<T> type) {
		synchronized (memberInjectionTokens) {
			@SuppressWarnings("unchecked")
			MembersInjectionToken<T> token = (MembersInjectionToken<T>) memberInjectionTokens
					.get(value);

			if (token == null) {
				token = new MembersInjectionToken<T>(this, value, type);
				memberInjectionTokens.put(value, token);
			}
			return token;
		}
	}

	public StandardInjectorConfiguration getConfig() {
		return config;
	}

}
