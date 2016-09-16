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

package com.github.ruediste.salta.jsr330.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.ImplementedBy;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.InjectorImpl;
import com.github.ruediste.salta.jsr330.JSR330InjectorConfiguration;
import com.github.ruediste.salta.jsr330.MembersInjector;
import com.github.ruediste.salta.jsr330.ProvidedBy;
import com.github.ruediste.salta.jsr330.SaltaModule;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Message;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.binder.SaltaMethodInterceptor;
import com.github.ruediste.salta.standard.binder.StandardAnnotatedConstantBindingBuilder;
import com.github.ruediste.salta.standard.binder.StandardBinder;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.reflect.TypeToken;

/**
 * Collects configuration information (primarily <i>bindings</i>) which will be
 * used to create an {@link Injector}. Guice provides this object to your
 * application's {@link SaltaModule} implementors so they may each contribute
 * their own bindings and other registrations.
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
 * want to use this if you prefer your {@link SaltaModule} class to serve as an
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
 * Besides {@link Singleton}, your Modules can contribute their own custom
 * scopes for use here as well.
 *
 * <pre>
 * bind(new TypeToken&lt;PaymentService&lt;CreditCard&gt;&gt;() {
 * }).to(CreditCardPaymentService.class);
 * </pre>
 *
 * This admittedly odd construct is the way to bind a parameterized type. It
 * tells Guice how to honor an injection request for an element of type
 * {@code PaymentService<CreditCard>}. The class
 * {@code CreditCardPaymentService} must implement the {@code PaymentService
 * <CreditCard>} interface. Guice cannot currently bind or inject a generic
 * type, such as {@code Set<E>}; all type parameters must be fully specified.
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
 * standard annotation, {@link Named @Named}. Because of Guice's library
 * support, binding by name is quite easier than in the arbitrary binding
 * annotation case we just saw. However, remember that these names will live in
 * a single flat namespace with all the other names used in your application.
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
 * The other methods of Binder such as {@link #bindScope}, {@link #install},
 * {@link #requestStaticInjection}, {@link #addError} and {@link #currentStage}
 * are not part of the Binding EDSL; you can learn how to use these in the usual
 * way, from the method documentation.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public class Binder {

    StandardBinder delegate;
    private JSR330InjectorConfiguration config;
    private InjectorImpl injector;

    public Binder(JSR330InjectorConfiguration config, InjectorImpl injector) {
        this.config = config;
        this.injector = injector;
        this.delegate = new StandardBinder(config.standardConfig, injector.getDelegate());
    }

    /**
     * Return the configuration modified by this Binder
     */
    public JSR330InjectorConfiguration config() {
        return config;
    }

    /**
     * Return the injector of this Binder. The injector is not initialized
     * during configuration, so it can not be used to create or inject
     * instances. But it is possible to store a reference for later use.
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * Binds a scope to an annotation.
     */
    public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        delegate.bindScope(annotationType, scope);
    }

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public <T> ScopedBindingBuilder<T> bind(TypeToken<T> type) {
        return new ScopedBindingBuilderImpl<>(delegate.bind(type));
    }

    public void close() {
        delegate.close();
    }

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return new AnnotatedBindingBuilderImpl<>(delegate.bind(type));
    }

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public StandardAnnotatedConstantBindingBuilder bindConstant() {
        return delegate.bindConstant();
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
        delegate.requestInjection(type, instance);
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
        delegate.requestInjection(instance);
    }

    /**
     * Upon successful creation, the {@link Injector} will inject static fields
     * and methods in the given classes.
     *
     * @param types
     *            for which static members will be injected
     */
    public void requestStaticInjection(Class<?>... types) {
        delegate.requestStaticInjection(types);
    }

    /**
     * Uses the given module to configure more bindings.
     */
    public void install(SaltaModule module) {
        config.modules.add(module);
        module.configure(this);
    }

    /**
     * Gets the current stage.
     */
    public Stage currentStage() {
        return delegate.currentStage();
    }

    /**
     * Records an error message which will be presented to the user at a later
     * time. Unlike throwing an exception, this enable us to continue
     * configuring the Injector and discover more errors. Uses
     * {@link String#format(String, Object[])} to insert the arguments into the
     * message.
     */
    public void addError(String message, Object... arguments) {
        delegate.addError(message, arguments);
    }

    /**
     * Records an exception, the full details of which will be logged, and the
     * message of which will be presented to the user at a later time. If your
     * Module calls something that you worry may fail, you should catch the
     * exception and pass it into this.
     */
    public void addError(Throwable t) {
        delegate.addError(t);
    }

    /**
     * Records an error message to be presented to the user at a later time.
     *
     * @since 2.0
     */
    public void addError(Message message) {
        delegate.addError(message);

    }

    /**
     * Returns the provider used to obtain instances for the given injection
     * key. The returned provider will not be valid until the {@link Injector}
     * has been created. The provider will throw an
     * {@code IllegalStateException} if you try to use it beforehand.
     *
     * @since 2.0
     */
    public <T> Provider<T> getProvider(CoreDependencyKey<T> key) {
        Supplier<T> result = delegate.getProvider(key);
        return new Provider<T>() {

            @Override
            public T get() {
                return result.get();
            }

            @Override
            public String toString() {
                return result.toString();
            }
        };
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
        return getProvider(DependencyKey.of(type));
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
        Consumer<T> inner = delegate.getMembersInjector(typeLiteral);
        return new MembersInjector<T>() {

            @Override
            public void injectMembers(T instance) {
                inner.accept(instance);
            }

            @Override
            public String toString() {
                return inner.toString();
            }
        };
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
     * Bind a creation rule allowing the creation of injection point specific
     * instances.
     */
    public void bindCreationRule(CreationRule rule) {
        config.standardConfig.creationPipeline.creationRules.add(rule);
    }

    public void bindMembersInjectorFactory(RecipeMembersInjectorFactory factory) {
        config().standardConfig.construction.membersInjectorFactories.add(factory);
    }

    /**
     * Binds method interceptor[s] to methods matched by class and method
     * matchers. A method is eligible for interception if:
     *
     * <ul>
     * <li>Salta created the instance the method is on</li>
     * <li>Neither the enclosing type nor the method is final</li>
     * <li>And the method is package-private, protected, or public</li>
     * </ul>
     *
     * @param keyMatcher
     *            matches keys the interceptor should apply to. For example:
     *            {@code only(Runnable.class)}.
     * @param methodMatcher
     *            matches methods the interceptor should apply to. For example:
     *            {@code annotatedWith(Transactional.class)}.
     * @param saltaInterceptor
     *            intercepts the method calls
     */
    public final void bindInterceptor(Matcher<? super CoreDependencyKey<?>> keyMatcher,
            Matcher<? super Method> methodMatcher, SaltaMethodInterceptor saltaInterceptor) {
        delegate.bindInterceptor(keyMatcher, methodMatcher, saltaInterceptor);
    }
}
