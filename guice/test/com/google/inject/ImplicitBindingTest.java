/**
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

package com.google.inject;

import junit.framework.TestCase;

import com.github.ruediste.salta.core.SaltaException;
import com.google.inject.name.Names;

/**
 * @author crazybob@google.com (Bob Lee)
 */
public class ImplicitBindingTest extends TestCase {

    public void testCircularDependency() throws CreationException {
        Injector injector = Guice.createInjector();
        try {
            Foo foo = injector.getInstance(Foo.class);
        } catch (SaltaException expected) {
            if (!expected.getMessage().contains("Circle"))
                throw expected;
        }
    }

    static class Foo {
        @Inject
        Bar bar;
    }

    static class Bar {
        final Foo foo;

        @Inject
        public Bar(Foo foo) {
            this.foo = foo;
        }
    }

    public void testDefaultImplementation() {
        Injector injector = Guice.createInjector();
        I i = injector.getInstance(I.class);
        i.go();
    }

    @ImplementedBy(IImpl.class)
    interface I {
        void go();
    }

    static class IImpl implements I {
        @Override
        public void go() {
        }
    }

    static class AlternateImpl implements I {
        @Override
        public void go() {
        }
    }

    public void testDefaultProvider() {
        Injector injector = Guice.createInjector();
        Provided provided = injector.getInstance(Provided.class);
        provided.go();
    }

    public void testBindingOverridesImplementedBy() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(I.class).to(AlternateImpl.class);
            }
        });
        assertEquals(AlternateImpl.class, injector.getInstance(I.class)
                .getClass());
    }

    @ProvidedBy(ProvidedProvider.class)
    interface Provided {
        void go();
    }

    public void testNoImplicitBindingIsCreatedForAnnotatedKeys() {
        try {
            Guice.createInjector().getInstance(
                    Key.get(I.class, Names.named("i")));
            fail();
        } catch (SaltaException expected) {
            if (!expected.getMessage().contains("No instance found for"))
                throw expected;
        }
    }

    static class ProvidedProvider implements Provider<Provided> {
        @Override
        public Provided get() {
            return new Provided() {
                @Override
                public void go() {
                }
            };
        }
    }

    static class Invalid {
        @Inject
        Valid a;
        @Inject
        JitValid b;
        @Inject
        InvalidProvidedBy c;

        @Inject
        Invalid(InvalidLinked a) {
        }

        @Inject
        void foo(InvalidInterface a) {
        }

    }

    @ImplementedBy(InvalidLinkedImpl.class)
    static interface InvalidLinked {
    }

    static class InvalidLinkedImpl implements InvalidLinked {
        @Inject
        InvalidLinked2 a;
    }

    @ImplementedBy(InvalidLinked2Impl.class)
    static interface InvalidLinked2 {
    }

    static class InvalidLinked2Impl implements InvalidLinked2 {
        @Inject
        InvalidLinked2Impl(Invalid2 a) {
        }
    }

    @ProvidedBy(InvalidProvidedByProvider.class)
    static interface InvalidProvidedBy {
    }

    static class InvalidProvidedByProvider implements
            Provider<InvalidProvidedBy> {
        @Inject
        InvalidProvidedBy2 a;

        @Override
        public InvalidProvidedBy get() {
            return null;
        }
    }

    @ProvidedBy(InvalidProvidedBy2Provider.class)
    static interface InvalidProvidedBy2 {
    }

    static class InvalidProvidedBy2Provider implements
            Provider<InvalidProvidedBy2> {
        @Inject
        Invalid2 a;

        @Override
        public InvalidProvidedBy2 get() {
            return null;
        }
    }

    static class Invalid2 {
        @Inject
        Invalid a;
    }

    interface InvalidInterface {
    }

    static class Valid {
        @Inject
        Valid2 a;
    }

    static class Valid2 {
    }

    static class JitValid {
        @Inject
        JitValid2 a;
    }

    static class JitValid2 {
    }

    static class TestStringProvider implements Provider<String> {
        static final String TEST_VALUE = "This is to verify it all works";

        @Override
        public String get() {
            return TEST_VALUE;
        }
    }

    static class RequiresProviderForSelfWithOtherType {
        private final Provider<RequiresProviderForSelfWithOtherType> selfProvider;
        private final String providedStringValue;

        @Inject
        RequiresProviderForSelfWithOtherType(String providedStringValue,
                Provider<RequiresProviderForSelfWithOtherType> selfProvider) {
            this.providedStringValue = providedStringValue;
            this.selfProvider = selfProvider;
        }

        public String getValue() {
            // Attempt to get another instance of ourself. This pattern
            // is possible for recursive processing.
            selfProvider.get();

            return providedStringValue;
        }
    }

    /**
     * Ensure that when we cleanup failed JIT bindings, we don't break. The test
     * here requires a sequence of JIT bindings: A-> B B -> C, A C -> A, D D not
     * JITable The problem was that C cleaned up A's binding and then handed
     * control back to B, which tried to continue processing A.. but A was
     * removed from the jitBindings Map, so it attempts to create a new JIT
     * binding for A, but we haven't yet finished constructing the first JIT
     * binding for A, so we get a recursive computation exception from
     * ComputingConcurrentHashMap.
     * 
     * We also throw in a valid JIT binding, E, to guarantee that if something
     * fails in this flow, it can be recreated later if it's not from a failed
     * sequence.
     */
    public void testRecursiveJitBindingsCleanupCorrectly() throws Exception {
        Injector injector = Guice.createInjector();
        try {
            injector.getInstance(A.class);
            fail("Expected failure");
        } catch (SaltaException expected) {
            if (!expected.getMessage().contains("Circle"))
                throw expected;
        }
    }

    static class A {
        @Inject
        public A(B b) {
        }
    }

    static class B {
        @Inject
        public B(C c, A a) {
        }
    }

    static class C {
        @Inject
        public C(A a, D d, E e) {
        }
    }

    static class D {
        public D(int i) {
        }
    }

    // Valid JITable binding
    static class E {
    }

    public void testProvidedByNonEmptyEnum() {
        NonEmptyEnum cardSuit = Guice.createInjector().getInstance(
                NonEmptyEnum.class);

        assertEquals(NonEmptyEnum.HEARTS, cardSuit);
    }

    public void testProvidedByEmptyEnum() {
        EmptyEnum emptyEnumValue = Guice.createInjector().getInstance(
                EmptyEnum.class);
        assertNull(emptyEnumValue);
    }

    @ProvidedBy(NonEmptyEnumProvider.class)
    enum NonEmptyEnum {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    static final class NonEmptyEnumProvider implements Provider<NonEmptyEnum> {
        @Override
        public NonEmptyEnum get() {
            return NonEmptyEnum.HEARTS;
        }
    }

    @ProvidedBy(EmptyEnumProvider.class)
    enum EmptyEnum {
    }

    static final class EmptyEnumProvider implements Provider<EmptyEnum> {
        @Override
        public EmptyEnum get() {
            return null;
        }
    }

    // An enum cannot be implemented by anything, so it should not be possible
    // to have a successful
    // binding when the enum is annotated with @ImplementedBy.
    public void testImplementedByEnum() {
        Injector injector = Guice.createInjector();
        try {
            injector.getInstance(EnumWithImplementedBy.class);
            fail("Expected failure");
        } catch (SaltaException expected) {

        }
    }

    @ImplementedBy(EnumWithImplementedByEnum.class)
    enum EnumWithImplementedBy {
    }

    private static class EnumWithImplementedByEnum {
    }
}
