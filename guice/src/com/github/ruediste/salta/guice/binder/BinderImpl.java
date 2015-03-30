package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.EnhancerFactory;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.RecipeEnhancer;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.guice.KeyAdapter;
import com.github.ruediste.salta.standard.ScopeImpl;
import com.github.ruediste.salta.standard.binder.StandardBinder;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancerWrapperImpl;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.ProvisionListener.ProvisionInvocation;

public class BinderImpl implements Binder {

	private com.github.ruediste.salta.standard.binder.StandardBinder delegate;
	private GuiceInjectorConfiguration config;

	public BinderImpl(
			com.github.ruediste.salta.standard.binder.StandardBinder delegate,
			GuiceInjectorConfiguration config) {
		this.delegate = delegate;
		this.config = config;
	}

	@Override
	public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
		checkForFrameworkTypes(typeLiteral.getRawType());
		return new AnnotatedBindingBuilderImpl<T>(delegate.bind(typeLiteral
				.getTypeToken()));
	}

	@Override
	public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
		checkForFrameworkTypes(type);
		return new AnnotatedBindingBuilderImpl<T>(delegate.bind(type));
	}

	private <T> void checkForFrameworkTypes(Class<T> type) {
		if (Provider.class.equals(type) || TypeLiteral.class.equals(type)
				|| MembersInjector.class.equals(type)
				|| Injector.class.equals(type)) {
			throw new com.github.ruediste.salta.core.SaltaException(
					"Binding to core guice framework type is not allowed: "
							+ type.getSimpleName() + ".");
		}
	}

	@Override
	public AnnotatedConstantBindingBuilder bindConstant() {
		return new AnnotatedConstantBindingBuilderImpl(delegate.bindConstant());
	}

	@Override
	public <T> void requestInjection(TypeLiteral<T> type, T instance) {
		delegate.requestInjection(type.getTypeToken(), instance);
	}

	@Override
	public void requestInjection(Object instance) {
		delegate.requestInjection(instance);
	}

	@Override
	public void requestStaticInjection(Class<?>... types) {
		delegate.requestStaticInjection(types);
	}

	@Override
	public void install(Module module) {
		config.modules.add(module);
		module.configure(this);
	}

	@Override
	public Stage currentStage() {
		switch (delegate.currentStage()) {
		case DEVELOPMENT:
			return Stage.DEVELOPMENT;
		case PRODUCTION:
			return Stage.PRODUCTION;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void addError(String message, Object... arguments) {
		delegate.addError(message, arguments);
	}

	@Override
	public void addError(Throwable t) {
		delegate.addError(t);
	}

	@Override
	public void addError(Message message) {
		delegate.addError(new com.github.ruediste.salta.standard.Message(
				message.getMessage(), message.getCause()));
	}

	@Override
	public <T> Provider<T> getProvider(Key<T> key) {
		Supplier<T> provider = delegate.getProvider(new KeyAdapter<>(key));
		return new Provider<T>() {
			@Override
			public T get() {
				return provider.get();
			}

			@Override
			public String toString() {
				return provider.toString();
			}
		};
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		Supplier<T> provider = delegate.getProvider(type);
		return new Provider<T>() {
			@Override
			public T get() {
				return provider.get();
			}

			@Override
			public String toString() {
				return provider.toString();
			}
		};
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
		Consumer<T> membersInjector = delegate.getMembersInjector(typeLiteral
				.getTypeToken());
		return new MembersInjector<T>() {
			@Override
			public void injectMembers(T instance) {
				membersInjector.accept(instance);
			}

			@Override
			public String toString() {
				return membersInjector.toString();
			}
		};
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return getMembersInjector(TypeLiteral.get(type));
	}

	@Override
	public void bindListener(Matcher<? super TypeToken<?>> typeMatcher,
			ProvisionListener... listeners) {

		for (int i = 0; i < listeners.length; i++) {
			ProvisionListener listener = listeners[i];
			delegate.getConfiguration().config.enhancerFactories
					.add(new EnhancerFactory() {

						final class ProvisionInvocationImpl extends
								ProvisionInvocation<Object> {

							Object value;
							boolean isProvisioned;
							private Supplier<Object> supplier;
							private TypeToken<?> type;

							ProvisionInvocationImpl(TypeToken<?> type,
									Supplier<Object> supplier) {
								this.type = type;
								this.supplier = supplier;

							}

							@Override
							public Object provision() {
								if (isProvisioned)
									throw new SaltaException(
											"provision() already called");
								Object result = supplier.get();
								value = result;
								isProvisioned = true;
								return result;
							}

							@Override
							public Binding<Object> getBinding() {
								return new BindingImpl<>(
										Key.get(type.getType()), null);
							}
						}

						@Override
						public RecipeEnhancer getEnhancer(
								RecipeCreationContext ctx,
								CoreDependencyKey<?> requestedKey) {
							if (!typeMatcher.matches(requestedKey.getType()))
								return null;
							return new RecipeEnhancerWrapperImpl(
									supplier -> {
										ProvisionInvocationImpl invocation = new ProvisionInvocationImpl(
												requestedKey.getType(),
												supplier);
										listener.onProvision(invocation);
										if (!invocation.isProvisioned)
											return supplier.get();
										else
											return invocation.value;
									});
						}
					});
		}
	}

	@Override
	public Binder withSource(Object source) {
		return this;
	}

	@Override
	public Binder skipSources(Class<?>... classesToSkip) {
		return this;
	}

	@Override
	public void requireExplicitBindings() {
		config.requireExplicitBindings = true;
	}

	@Override
	public void requireAtInjectOnConstructors() {
		config.requireAtInjectOnConstructors = true;
	}

	@Override
	public void requireExactBindingAnnotations() {
		config.requireExactBindingAnnotations = true;

	}

	@Override
	public void disableCircularProxies() {
		// Salta has no circular proxies
	}

	@Override
	public void bindScope(Class<? extends Annotation> annotationType,
			Scope scope) {
		delegate.bindScope(annotationType, new ScopeImpl(new GuiceScopeAdapter(
				scope)));
	}

	@Override
	public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
		return new LinkedBindingBuilderImpl<>(delegate.bind(
				key.getTypeLiteral().getTypeToken()).annotatedWith(
				key.getAnnotation()));
	}

	@Override
	public GuiceInjectorConfiguration getGuiceConfiguration() {
		return config;
	}

	@Override
	public StandardBinder getDelegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return "Binder";
	}

	public void close() {
		delegate.close();
	}
}
