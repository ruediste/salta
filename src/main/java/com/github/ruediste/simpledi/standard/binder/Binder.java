/*
 * Copyright (C) 2014 Ruedi Steinmann
 * 
 * Copyright (C) 2007 Google Inc.
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

package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.proxy.ProxyRefDispatcher;

import com.github.ruediste.simpledi.AbstractModule;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.MembersInjector;
import com.github.ruediste.simpledi.core.Message;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.core.Scope;
import com.github.ruediste.simpledi.core.Stage;
import com.github.ruediste.simpledi.matchers.Matcher;
import com.github.ruediste.simpledi.standard.FillDefaultsRecipeCreationStep;
import com.github.ruediste.simpledi.standard.Module;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;
import com.google.common.reflect.TypeToken;

/**
 * Collects configuration information (primarily <i>bindings</i>) which will be
 * used to create an {@link Injector}. Guice provides this object to your
 * application's {@link Module} implementors so they may each contribute their
 * own bindings and other registrations.
 *
 * <h3>The Guice Binding EDSL</h3>
 *
 * Guice uses an <i>embedded domain-specific language</i>, or EDSL, to help you
 * create bindings simply and readably. This approach is great for overall
 * usability, but it does come with a small cost: <b>it is difficult to learn
 * how to use the Binding EDSL by reading method-level javadocs</b>. Instead,
 * you should consult the series of examples below. To save space, these
 * examples omit the opening {@code binder}, just as you will if your module
 * extends {@link AbstractModule}.
 *
 * <pre>
 * bind(ServiceImpl.class);
 * </pre>
 *
 * This statement does essentially nothing; it "binds the {@code ServiceImpl}
 * class to itself" and does not change Guice's default behavior. You may still
 * want to use this if you prefer your {@link Module} class to serve as an
 * explicit <i>manifest</i> for the services it provides. Also, in rare cases,
 * Guice may be unable to validate a binding at injector creation time unless it
 * is given explicitly.
 *
 * <pre>
 * bind(Service.class).to(ServiceImpl.class);
 * </pre>
 *
 * Specifies that a request for a {@code Service} instance with no binding
 * annotations should be treated as if it were a request for a
 * {@code ServiceImpl} instance. This <i>overrides</i> the function of any
 * {@link ImplementedBy @ImplementedBy} or {@link ProvidedBy @ProvidedBy}
 * annotations found on {@code Service}, since Guice will have already
 * "moved on" to {@code ServiceImpl} before it reaches the point when it starts
 * looking for these annotations.
 *
 * <pre>
 * bind(Service.class).toProvider(ServiceProvider.class);
 * </pre>
 *
 * In this example, {@code ServiceProvider} must extend or implement
 * {@code Provider<Service>}. This binding specifies that Guice should resolve
 * an unannotated injection request for {@code Service} by first resolving an
 * instance of {@code ServiceProvider} in the regular way, then calling
 * {@link Provider#get get()} on the resulting Provider instance to obtain the
 * {@code Service} instance.
 *
 * <p>
 * The {@link Provider} you use here does not have to be a "factory"; that is, a
 * provider which always <i>creates</i> each instance it provides. However, this
 * is generally a good practice to follow. You can then use Guice's concept of
 * {@link Scope scopes} to guide when creation should happen --
 * "letting Guice work for you".
 *
 * <pre>
 * bind(Service.class).annotatedWith(Red.class).to(ServiceImpl.class);
 * </pre>
 *
 * Like the previous example, but only applies to injection requests that use
 * the binding annotation {@code @Red}. If your module also includes bindings
 * for particular <i>values</i> of the {@code @Red} annotation (see below), then
 * this binding will serve as a "catch-all" for any values of {@code @Red} that
 * have no exact match in the bindings.
 * 
 * <pre>
 * bind(ServiceImpl.class).in(Singleton.class);
 * // or, alternatively
 * bind(ServiceImpl.class).in(Scopes.SINGLETON);
 * </pre>
 *
 * Either of these statements places the {@code ServiceImpl} class into
 * singleton scope. Guice will create only one instance of {@code ServiceImpl}
 * and will reuse it for all injection requests of this type. Note that it is
 * still possible to bind another instance of {@code ServiceImpl} if the second
 * binding is qualified by an annotation as in the previous example. Guice is
 * not overly concerned with <i>preventing</i> you from creating multiple
 * instances of your "singletons", only with <i>enabling</i> your application to
 * share only one instance if that's all you tell Guice you need.
 *
 * <p>
 * <b>Note:</b> a scope specified in this way <i>overrides</i> any scope that
 * was specified with an annotation on the {@code ServiceImpl} class.
 * 
 * <p>
 * Besides {@link Singleton}/{@link Scopes#SINGLETON}, there are
 * servlet-specific scopes available in
 * {@code com.google.inject.servlet.ServletScopes}, and your Modules can
 * contribute their own custom scopes for use here as well.
 *
 * <pre>
 * bind(new TypeToken&lt;PaymentService&lt;CreditCard&gt;&gt;() {
 * }).to(CreditCardPaymentService.class);
 * </pre>
 *
 * This admittedly odd construct is the way to bind a parameterized type. It
 * tells Guice how to honor an injection request for an element of type
 * {@code PaymentService<CreditCard>}. The class
 * {@code CreditCardPaymentService} must implement the
 * {@code PaymentService<CreditCard>} interface. Guice cannot currently bind or
 * inject a generic type, such as {@code Set<E>}; all type parameters must be
 * fully specified.
 *
 * <pre>
 * bind(Service.class).toInstance(new ServiceImpl());
 * // or, alternatively
 * bind(Service.class).toInstance(SomeLegacyRegistry.getService());
 * </pre>
 *
 * In this example, your module itself, <i>not Guice</i>, takes responsibility
 * for obtaining a {@code ServiceImpl} instance, then asks Guice to always use
 * this single instance to fulfill all {@code Service} injection requests. When
 * the {@link Injector} is created, it will automatically perform field and
 * method injection for this instance, but any injectable constructor on
 * {@code ServiceImpl} is simply ignored. Every object instance is injected only
 * once, even if it is bound multiple times. Note that using this approach
 * results in "eager loading" behavior that you can't control.
 *
 * <pre>
 * bindConstant().annotatedWith(ServerHost.class).to(args[0]);
 * </pre>
 *
 * Sets up a constant binding. Constant injections must always be annotated.
 * When a constant binding's value is a string, it is eligile for conversion to
 * all primitive types, to {@link Enum#valueOf(Class, String) all enums}, and to
 * {@link Class#forName class literals}. Conversions for other types can be
 * configured using {@link #convertToTypes(Matcher, TypeConverter)
 * convertToTypes()}.
 *
 * <pre>
 *   {@literal @}Color("red") Color red; // A member variable (field)
 *    . . .
 *     red = MyModule.class.getDeclaredField("red").getAnnotation(Color.class);
 *     bind(Service.class).annotatedWith(red).to(RedService.class);
 * </pre>
 *
 * If your binding annotation has parameters you can apply different bindings to
 * different specific values of your annotation. Getting your hands on the right
 * instance of the annotation is a bit of a pain -- one approach, shown above,
 * is to apply a prototype annotation to a field in your module class, so that
 * you can read this annotation instance and give it to Guice.
 *
 * <pre>
 * bind(Service.class).annotatedWith(Names.named(&quot;blue&quot;)).to(BlueService.class);
 * </pre>
 *
 * Differentiating by names is a common enough use case that we provided a
 * standard annotation, {@link com.google.inject.name.Named @Named}. Because of
 * Guice's library support, binding by name is quite easier than in the
 * arbitrary binding annotation case we just saw. However, remember that these
 * names will live in a single flat namespace with all the other names used in
 * your application.
 *
 * <pre>
 * Constructor&lt;T&gt; loneCtor = getLoneCtorFromServiceImplViaReflection();
 * bind(ServiceImpl.class).toConstructor(loneCtor);
 * </pre>
 *
 * In this example, we directly tell Guice which constructor to use in a
 * concrete class implementation. It means that we do not need to place
 * {@literal @}Inject on any of the constructors and that Guice treats the
 * provided constructor as though it were annotated so. It is useful for cases
 * where you cannot modify existing classes and is a bit simpler than using a
 * {@link Provider}.
 *
 * <p>
 * The above list of examples is far from exhaustive. If you can think of how
 * the concepts of one example might coexist with the concepts from another, you
 * can most likely weave the two together. If the two concepts make no sense
 * with each other, you most likely won't be able to do it. In a few cases Guice
 * will let something bogus slip by, and will then inform you of the problems at
 * runtime, as soon as you try to create your Injector.
 *
 * <p>
 * The other methods of Binder such as {@link #bindScope},
 * {@link #bindInterceptor}, {@link #install}, {@link #requestStaticInjection},
 * {@link #addError} and {@link #currentStage} are not part of the Binding EDSL;
 * you can learn how to use these in the usual way, from the method
 * documentation.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public class Binder {

	private final class MembersInjectorImpl<T> implements MembersInjector<T> {
		public Injector injector;
		private TypeToken<T> typeLiteral;

		public MembersInjectorImpl(TypeToken<T> typeLiteral) {
			this.typeLiteral = typeLiteral;
		}

		@Override
		public void injectMembers(T instance) {

			if (injector == null) {
				throw new ProvisionException(
						"Injector has to be created before using this MembersProvider");
			}
			injector.injectMembers(typeLiteral, instance);
		}
	}

	private final class ProviderImpl<T> implements Provider<T> {
		public Injector injector;
		private Dependency<T> dependendcy;

		public ProviderImpl(Dependency<T> dependendcy) {
			this.dependendcy = dependendcy;
		}

		@Override
		public T get() {
			if (injector == null) {
				throw new ProvisionException(
						"Injector has to be created before using this provider");
			}
			return injector.createInstance(dependendcy);
		}
	}

	private StandardInjectorConfiguration config;

	public Binder(StandardInjectorConfiguration config) {
		this.config = config;

	}

	/**
	 * Return the configuration modified by this Binder
	 */
	public StandardInjectorConfiguration getConfiguration() {
		return config;
	}

	/**
	 * Binds method interceptor[s] to methods matched by class and method
	 * matchers. A method is eligible for interception if:
	 *
	 * <ul>
	 * <li>Guice created the instance the method is on</li>
	 * <li>Neither the enclosing type nor the method is final</li>
	 * <li>And the method is package-private, protected, or public</li>
	 * </ul>
	 *
	 * @param classMatcher
	 *            matches classes the interceptor should apply to. For example:
	 *            {@code only(Runnable.class)}.
	 * @param methodMatcher
	 *            matches methods the interceptor should apply to. For example:
	 *            {@code annotatedWith(Transactional.class)}.
	 * @param interceptors
	 *            to bind. The interceptors are called in the order they are
	 *            given. CgLib Callbacks: {@link Dispatcher}, {@link FixedValue}
	 *            , {@link InvocationHandler}, {@link LazyLoader},
	 *            {@link MethodInterceptor}, {@link NoOp},
	 *            {@link ProxyRefDispatcher}
	 */
	public void bindInterceptor(Matcher<? super Class<?>> classMatcher,
			Matcher<? super Method> methodMatcher, Callback... interceptors) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Binds a scope to an annotation.
	 */
	public void bindScope(Class<? extends Annotation> annotationType,
			Scope scope) {

		// put the annotation to the map for further use by other binders
		config.scopeAnnotationMap.put(annotationType, scope);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public <T> AnnotatedBindingBuilder<T> bind(TypeToken<T> type) {
		StandardStaticBinding binding1 = new StandardStaticBinding();
		binding1.dependencyMatcher = x -> x.type.equals(type);
		binding1.recipeCreationSteps.add(new FillDefaultsRecipeCreationStep(
				config, type));
		StandardStaticBinding binding = binding1;
		config.config.staticBindings.add(binding);

		return new AnnotatedBindingBuilder<>(binding, Dependency.of(type),
				config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
		return bind(TypeToken.of(type));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public AnnotatedConstantBindingBuilder bindConstant() {
		return new AnnotatedConstantBindingBuilder(config);
	}

	/**
	 * Upon successful creation, the {@link Injector} will inject instance
	 * fields and methods of the given object.
	 *
	 * @param type
	 *            of instance
	 * @param instance
	 *            for which members will be injected
	 * @since 2.0
	 */
	public <T> void requestInjection(TypeToken<T> type, T instance) {
		config.config.dynamicInitializers.add(x -> x.injectMembers(type,
				instance));
	}

	/**
	 * Upon successful creation, the {@link Injector} will inject instance
	 * fields and methods of the given object.
	 *
	 * @param instance
	 *            for which members will be injected
	 * @since 2.0
	 */
	public void requestInjection(Object instance) {
		config.config.dynamicInitializers.add(x -> x.injectMembers(instance));
	}

	/**
	 * Upon successful creation, the {@link Injector} will inject static fields
	 * and methods in the given classes.
	 *
	 * @param types
	 *            for which static members will be injected
	 */
	public void requestStaticInjection(Class<?>... types) {
		for (Class<?> type : types) {
			config.requestedStaticInjections.add(type);
		}
	}

	/**
	 * Uses the given module to configure more bindings.
	 */
	public void install(Module module) {
		module.configure(this);
	}

	/**
	 * Gets the current stage.
	 */
	public Stage currentStage() {
		return config.config.stage;
	}

	/**
	 * Records an error message which will be presented to the user at a later
	 * time. Unlike throwing an exception, this enable us to continue
	 * configuring the Injector and discover more errors. Uses
	 * {@link String#format(String, Object[])} to insert the arguments into the
	 * message.
	 */
	public void addError(String message, Object... arguments) {
		config.config.errorMessages.add(new Message(String.format(message,
				arguments)));
	}

	/**
	 * Records an exception, the full details of which will be logged, and the
	 * message of which will be presented to the user at a later time. If your
	 * Module calls something that you worry may fail, you should catch the
	 * exception and pass it into this.
	 */
	public void addError(Throwable t) {
		config.config.errorMessages.add(new Message("", t));
	}

	/**
	 * Records an error message to be presented to the user at a later time.
	 *
	 * @since 2.0
	 */
	public void addError(Message message) {
		config.config.errorMessages.add(message);

	}

	/**
	 * Returns the provider used to obtain instances for the given injection
	 * key. The returned provider will not be valid until the {@link Injector}
	 * has been created. The provider will throw an
	 * {@code IllegalStateException} if you try to use it beforehand.
	 *
	 * @since 2.0
	 */
	public <T> Provider<T> getProvider(Dependency<T> key) {
		ProviderImpl<T> provider = new ProviderImpl<T>(key);
		config.config.staticInitializers.add(x -> provider.injector = x);
		return provider;
	}

	/**
	 * Returns the provider used to obtain instances for the given injection
	 * type. The returned provider will not be valid until the {@link Injector}
	 * has been created. The provider will throw an
	 * {@code IllegalStateException} if you try to use it beforehand.
	 *
	 * @since 2.0
	 */
	public <T> Provider<T> getProvider(Class<T> type) {
		return getProvider(Dependency.of(type));
	}

	/**
	 * Returns the members injector used to inject dependencies into methods and
	 * fields on instances of the given type {@code T}. The returned members
	 * injector will not be valid until the main {@link Injector} has been
	 * created. The members injector will throw an {@code IllegalStateException}
	 * if you try to use it beforehand.
	 *
	 * @param typeLiteral
	 *            type to get members injector for
	 * @since 2.0
	 */
	public <T> MembersInjector<T> getMembersInjector(TypeToken<T> typeLiteral) {
		MembersInjectorImpl<T> membersInjector = new MembersInjectorImpl<T>(
				typeLiteral);
		config.config.staticInitializers
				.add(injector -> membersInjector.injector = injector);
		return membersInjector;
	}

	/**
	 * Returns the members injector used to inject dependencies into methods and
	 * fields on instances of the given type {@code T}. The returned members
	 * injector will not be valid until the main {@link Injector} has been
	 * created. The members injector will throw an {@code IllegalStateException}
	 * if you try to use it beforehand.
	 *
	 * @param type
	 *            type to get members injector for
	 * @since 2.0
	 */
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return getMembersInjector(TypeToken.of(type));
	}

	/**
	 * Prevents Guice from constructing a {@link Proxy} when a circular
	 * dependency is found. By default, circular proxies are not disabled.
	 * <p>
	 * If a parent injector disables circular proxies, then all child injectors
	 * (and private modules within that injector) also disable circular proxies.
	 * If a parent does not disable circular proxies, a child injector or
	 * private module may optionally declare itself as disabling circular
	 * proxies. If it does, the behavior is limited only to that child or any
	 * grandchildren. No siblings of the child will disable circular proxies.
	 * 
	 * @since 3.0
	 */
	public void disableCircularProxies() {
		config.config.disableCircularProxies = true;
	}

	/**
	 * Requires that a {@literal @}{@link Inject} annotation exists on a
	 * constructor in order for Guice to consider it an eligible injectable
	 * class. By default, Guice will inject classes that have a no-args
	 * constructor if no {@literal @}{@link Inject} annotation exists on any
	 * constructor.
	 * <p>
	 * If the class is bound using {@link LinkedBindingBuilder#toConstructor},
	 * Guice will still inject that constructor regardless of annotations.
	 *
	 * @since 4.0
	 */
	public void requireAtInjectOnConstructors() {
		config.config.requireAtInjectOnConstructors = true;
	}

}
