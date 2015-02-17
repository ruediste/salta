package com.github.ruediste.salta.guice.binder;

import java.util.function.BiFunction;

import com.github.ruediste.salta.guice.KeyAdapter;
import com.github.ruediste.salta.guice.ModuleAdapter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeListener;

public class BinderImpl implements Binder {

	private com.github.ruediste.salta.standard.binder.Binder delegate;

	public BinderImpl(com.github.ruediste.salta.standard.binder.Binder delegate) {
		this.delegate = delegate;
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
	public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher,
			TypeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindListener(Matcher<? super Binding<?>> bindingMatcher,
			ProvisionListener... listeners) {

		@SuppressWarnings("unchecked")
		BiFunction<TypeToken<?>, Object, Object>[] saltaListeners = new BiFunction[listeners.length];

		for (int i = 0; i < listeners.length; i++) {
			ProvisionListener listener = listeners[i];
			saltaListeners[i] = (t, x) -> {

			};
		}
		delegate.bindListener(
				type -> bindingMatcher.matches(new BindingImpl<>(type)),
				saltaListener);
	}

	@Override
	public Binder withSource(Object source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder skipSources(Class... classesToSkip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrivateBinder newPrivateBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void requireExplicitBindings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableCircularProxies() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requireAtInjectOnConstructors() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requireExactBindingAnnotations() {
		// TODO Auto-generated method stub

	}

}
