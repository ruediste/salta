package com.github.ruediste.salta.guice.binder;

import com.github.ruediste.salta.guice.KeyAdapter;
import com.github.ruediste.salta.guice.ModuleAdapter;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.spi.Message;

public class BinderImpl implements Binder {

	private com.github.ruediste.salta.standard.binder.Binder delegate;
	private GuiceInjectorConfiguration config;

	public BinderImpl(
			com.github.ruediste.salta.standard.binder.Binder delegate,
			GuiceInjectorConfiguration config) {
		this.delegate = delegate;
		this.config = config;
	}

	@Override
	public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
		return new AnnotatedBindingBuilderImpl<T>(delegate.bind(typeLiteral
				.getTypeToken()));
	}

	@Override
	public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
		return new AnnotatedBindingBuilderImpl<T>(delegate.bind(type));
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
		delegate.install(new ModuleAdapter(module));
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
		return delegate.getProvider(new KeyAdapter<>(key))::get;
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		return delegate.getProvider(type)::get;
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
		return delegate.getMembersInjector(typeLiteral.getTypeToken())::injectMembers;
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return delegate.getMembersInjector(type)::injectMembers;
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

}
