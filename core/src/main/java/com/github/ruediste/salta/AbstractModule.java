/**
 * Copyright (C) 2014 Ruedi Steinmann
 * 
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ruediste.salta;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Provider;

import net.sf.cglib.proxy.Callback;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.MembersInjector;
import com.github.ruediste.salta.standard.Message;
import com.github.ruediste.salta.standard.Module;
import com.github.ruediste.salta.standard.Scope;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.binder.AnnotatedBindingBuilder;
import com.github.ruediste.salta.standard.binder.AnnotatedConstantBindingBuilder;
import com.github.ruediste.salta.standard.binder.Binder;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * A support class for {@link Module}s which reduces repetition and results in a
 * more readable configuration. Simply extend this class, implement
 * {@link #configure()}, and call the inherited methods which mirror those found
 * in {@link Binder}. For example:
 *
 * <pre>
 * public class MyModule extends AbstractModule {
 * 	protected void configure() {
 * 		bind(Service.class).to(ServiceImpl.class).in(Singleton.class);
 * 		bind(CreditCardPaymentService.class);
 * 		bind(PaymentService.class).to(CreditCardPaymentService.class);
 * 		bindConstant().annotatedWith(Names.named(&quot;port&quot;)).to(8080);
 * 	}
 * }
 * </pre>
 *
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class AbstractModule implements Module {

	Binder binder;

	@Override
	public final synchronized void configure(Binder binder) {
		checkState(this.binder == null, "Re-entry is not allowed.");

		binder.getConfiguration().modules.add(this);

		this.binder = checkNotNull(binder, "builder");
		try {
			configure();
		} finally {
			this.binder = null;
		}
	}

	/**
	 * Configures a {@link Binder} via the exposed methods.
	 */
	protected abstract void configure();

	/**
	 * Gets direct access to the underlying {@code Binder}.
	 */
	protected Binder binder() {
		checkState(binder != null,
				"The binder can only be used inside configure()");
		return binder;
	}

	/**
	 * @see Binder#bindScope(Class, Scope)
	 */
	protected void bindScope(Class<? extends Annotation> scopeAnnotation,
			Scope scope) {
		binder().bindScope(scopeAnnotation, scope);
	}

	/**
	 * @see Binder#bind(Class)
	 */
	protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
		return binder().bind(clazz);
	}

	/**
	 * @see Binder#bindConstant()
	 */
	protected AnnotatedConstantBindingBuilder bindConstant() {
		return binder().bindConstant();
	}

	/**
	 * @see Binder#install(Module)
	 */
	protected void install(Module module) {
		binder().install(module);
	}

	/**
	 * @see Binder#addError(String, Object[])
	 */
	protected void addError(String message, Object... arguments) {
		binder().addError(message, arguments);
	}

	/**
	 * @see Binder#addError(Throwable)
	 */
	protected void addError(Throwable t) {
		binder().addError(t);
	}

	/**
	 * @see Binder#addError(Message)
	 * @since 2.0
	 */
	protected void addError(Message message) {
		binder().addError(message);
	}

	/**
	 * @see Binder#requestInjection(Object)
	 * @since 2.0
	 */
	protected void requestInjection(Object instance) {
		binder().requestInjection(instance);
	}

	/**
	 * @see Binder#requestStaticInjection(Class[])
	 */
	protected void requestStaticInjection(Class<?>... types) {
		binder().requestStaticInjection(types);
	}

	/**
	 * @see Binder#bindInterceptor(com.google.inject.matcher.Matcher,
	 *      com.google.inject.matcher.Matcher,
	 *      org.aopalliance.intercept.MethodInterceptor[])
	 */
	protected void bindInterceptor(Matcher<? super Class<?>> classMatcher,
			Matcher<? super Method> methodMatcher, Callback... interceptors) {
		binder().bindInterceptor(classMatcher, methodMatcher, interceptors);
	}

	/**
	 * @see Binder#getProvider(CoreDependencyKey)
	 * @since 2.0
	 */
	protected <T> Provider<T> getProvider(CoreDependencyKey<T> key) {
		return binder().getProvider(key);
	}

	/**
	 * @see Binder#getProvider(Class)
	 * @since 2.0
	 */
	protected <T> Provider<T> getProvider(Class<T> type) {
		return binder().getProvider(type);
	}

	/**
	 * @see Binder#currentStage()
	 * @since 2.0
	 */
	protected Stage currentStage() {
		return binder().currentStage();
	}

	/**
	 * @see Binder#getMembersInjector(Class)
	 * @since 2.0
	 */
	protected <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return binder().getMembersInjector(type);
	}

	/**
	 * @see Binder#getMembersInjector(TypeLiteral)
	 * @since 2.0
	 */
	protected <T> MembersInjector<T> getMembersInjector(TypeToken<T> type) {
		return binder().getMembersInjector(type);
	}

	protected StandardInjectorConfiguration getConfiguration() {
		return binder().getConfiguration();
	}
}
