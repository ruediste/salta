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

package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.EnhancerFactory;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.RecipeEnhancer;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Message;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeEnhancerImpl;
import com.google.common.reflect.TypeToken;

/**
 * Since many API types are not present in the core, the documentation has been
 * moved to the Binder classes of the JSR330 or the Guice API.
 * */
public class StandardBinder {

    protected StandardInjectorConfiguration config;
    protected StandardInjector injector;

    protected StandardBindingBuilderImpl<?> currentBindingBuilder;

    public StandardBinder(StandardInjectorConfiguration config,
            StandardInjector injector) {
        this.config = config;
        this.injector = injector;

    }

    /**
     * Return the configuration modified by this Binder
     */
    public StandardInjectorConfiguration getConfiguration() {
        return config;
    }

    /**
     * Return the injector of this Binder. The injector is not initialized
     * during configuration, so it can not be used to create or inject
     * instances. But it is possible to store a reference for later use.
     */
    public StandardInjector getInjector() {
        return injector;
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
     * See the EDSL examples at {@link StandardBinder}.
     */
    public <T> StandardAnnotatedBindingBuilder<T> bind(TypeToken<T> type) {
        if (currentBindingBuilder != null)
            currentBindingBuilder.register();
        StandardBindingBuilderImpl<T> tmp = createBindingBuilder(type);
        currentBindingBuilder = tmp;
        return tmp;
    }

    /**
     * This metod is used by {@link #bind(TypeToken)} to instantiate a binding
     * builder and can be used to instantiate a subclass
     */
    protected <T> StandardBindingBuilderImpl<T> createBindingBuilder(
            TypeToken<T> type) {
        return new StandardBindingBuilderImpl<>(
                CoreDependencyKey.typeMatcher(type), type, config, injector);
    }

    public void close() {
        if (currentBindingBuilder != null)
            currentBindingBuilder.register();
    }

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public <T> StandardAnnotatedBindingBuilder<T> bind(Class<T> type) {
        return bind(TypeToken.of(type));
    }

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public StandardAnnotatedConstantBindingBuilder bindConstant() {
        return new StandardAnnotatedConstantBindingBuilder(config);
    }

    /**
     * Upon successful creation, the {@link StandardInjector} will inject
     * instance fields and methods of the given object.
     *
     * @param type
     *            of instance
     * @param instance
     *            for which members will be injected
     * @since 2.0
     */
    public <T> void requestInjection(TypeToken<T> type, T instance) {
        config.dynamicInitializers.add(() -> injector.injectMembers(type,
                instance));
    }

    /**
     * Upon successful creation, the {@link StandardInjector} will inject
     * instance fields and methods of the given object.
     *
     * @param instance
     *            for which members will be injected
     * @since 2.0
     */
    public void requestInjection(Object instance) {
        config.dynamicInitializers.add(() -> injector.injectMembers(instance));
    }

    /**
     * Upon successful creation, the {@link StandardInjector} will inject static
     * fields and methods in the given classes.
     *
     * @param types
     *            for which static members will be injected
     */
    public void requestStaticInjection(Class<?>... types) {
        for (Class<?> type : types) {
            if (type.isInterface()) {
                throw new SaltaException(
                        "Requested static injection of "
                                + type
                                + ", but interfaces do not have static injection points.");
            }
            config.requestedStaticInjections.add(type);
        }
    }

    /**
     * Gets the current stage.
     */
    public Stage currentStage() {
        return config.stage;
    }

    /**
     * Records an error message which will be presented to the user at a later
     * time. Unlike throwing an exception, this enable us to continue
     * configuring the Injector and discover more errors. Uses
     * {@link String#format(String, Object[])} to insert the arguments into the
     * message.
     */
    public void addError(String message, Object... arguments) {
        config.errorMessages
                .add(new Message(String.format(message, arguments)));
    }

    /**
     * Records an exception, the full details of which will be logged, and the
     * message of which will be presented to the user at a later time. If your
     * Module calls something that you worry may fail, you should catch the
     * exception and pass it into this.
     */
    public void addError(Throwable t) {
        config.errorMessages.add(new Message("", t));
    }

    /**
     * Records an error message to be presented to the user at a later time.
     *
     * @since 2.0
     */
    public void addError(Message message) {
        config.errorMessages.add(message);

    }

    /**
     * Returns the provider used to obtain instances for the given injection
     * key. The returned provider will not be valid until the
     * {@link StandardInjector} has been created. The provider will throw an
     * {@code IllegalStateException} if you try to use it beforehand.
     *
     * @since 2.0
     */
    public <T> Supplier<T> getProvider(CoreDependencyKey<T> key) {
        return injector.getProvider(key);
    }

    /**
     * Returns the provider used to obtain instances for the given injection
     * type. The returned provider will not be valid until the
     * {@link StandardInjector} has been created. The provider will throw an
     * {@code IllegalStateException} if you try to use it beforehand.
     *
     * @since 2.0
     */
    public <T> Supplier<T> getProvider(Class<T> type) {
        return getProvider(DependencyKey.of(type));
    }

    /**
     * Returns the members injector used to inject dependencies into methods and
     * fields on instances of the given type {@code T}. The returned members
     * injector will not be valid until the main {@link StandardInjector} has
     * been created. The members injector will throw an
     * {@code IllegalStateException} if you try to use it beforehand.
     *
     * @param typeLiteral
     *            type to get members injector for
     * @since 2.0
     */
    public <T> Consumer<T> getMembersInjector(TypeToken<T> typeLiteral) {
        return new Consumer<T>() {
            volatile boolean injected;
            Consumer<T> inner;

            @Override
            public void accept(T instance) {
                if (!injected) {
                    synchronized (injector.getCoreInjector().recipeLock) {
                        inner = injector.getMembersInjector(typeLiteral);
                        injected = true;
                    }
                }
                inner.accept(instance);
            }

            @Override
            public String toString() {
                return "MembersInjector<" + typeLiteral + ">";
            }
        };
    }

    /**
     * Returns the members injector used to inject dependencies into methods and
     * fields on instances of the given type {@code T}. The returned members
     * injector will not be valid until the main {@link StandardInjector} has
     * been created. The members injector will throw an
     * {@code IllegalStateException} if you try to use it beforehand.
     *
     * @param type
     *            type to get members injector for
     * @since 2.0
     */
    public <T> Consumer<T> getMembersInjector(Class<T> type) {
        return getMembersInjector(TypeToken.of(type));
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
    public final void bindInterceptor(
            Matcher<? super CoreDependencyKey<?>> keyMatcher,
            Matcher<? super Method> methodMatcher,
            SaltaMethodInterceptor saltaInterceptor) {

        config.config.enhancerFactories.add(new EnhancerFactory() {

            @Override
            public RecipeEnhancer getEnhancer(RecipeCreationContext ctx,
                    CoreDependencyKey<?> requestedKey) {

                if (keyMatcher.matches(requestedKey)) {

                    boolean found = false;
                    typeLoop: for (TypeToken<?> t : requestedKey.getType()
                            .getTypes()) {
                        for (Method m : t.getRawType().getDeclaredMethods()) {
                            if (Modifier.isStatic(m.getModifiers()))
                                continue;
                            if (Modifier.isPrivate(m.getModifiers()))
                                continue;
                            if (methodMatcher.matches(m)) {
                                found = true;
                                break typeLoop;
                            }
                        }
                    }
                    if (!found)
                        return null;

                    return new RecipeEnhancerImpl(
                            inner -> {
                                if (inner == null)
                                    return null;

                                try {
                                    Enhancer e = new Enhancer();
                                    LazyLoader loader = new LazyLoader() {

                                        @Override
                                        public Object loadObject()
                                                throws Exception {
                                            return inner;
                                        }
                                    };
                                    MethodInterceptor interceptor = new MethodInterceptor() {

                                        @Override
                                        public Object intercept(Object obj,
                                                Method method, Object[] args,
                                                MethodProxy proxy)
                                                throws Throwable {
                                            return saltaInterceptor.intercept(
                                                    inner, method, args, proxy);
                                        }
                                    };
                                    e.setSuperclass(requestedKey.getRawType());
                                    e.setCallbacks(new Callback[] { loader,
                                            interceptor });
                                    e.setCallbackFilter(new CallbackFilter() {

                                        @Override
                                        public int accept(Method method) {
                                            if (methodMatcher.matches(method))
                                                return 1;
                                            else
                                                return 0;
                                        }
                                    });
                                    return e.create();
                                } catch (Throwable t) {
                                    throw new SaltaException(
                                            "Error while creating proxy to enhance "
                                                    + requestedKey, t);
                                }
                            });
                }
                return null;
            }
        });
    }
}
