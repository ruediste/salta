/**
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

package com.google.inject;

import static com.google.inject.Asserts.asModuleChain;
import static com.google.inject.Asserts.assertContains;
import static com.google.inject.Asserts.assertNotSerializable;
import static com.google.inject.Asserts.getDeclaringSourcePart;
import static com.google.inject.Asserts.isIncludeStackTraceOff;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.junit.Ignore;
import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.google.common.collect.Lists;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;
import com.google.inject.util.Providers;

/**
 * @author crazybob@google.com (Bob Lee)
 */
public class BinderTest {

    private final List<LogRecord> logRecords = Lists.newArrayList();
    private final Handler fakeHandler = new Handler() {
        @Override
        public void publish(LogRecord logRecord) {
            logRecords.add(logRecord);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    };

    Provider<Foo> fooProvider;

    @Test
    public void testProviderFromBinder() {
        Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                fooProvider = binder.getProvider(Foo.class);

                try {
                    fooProvider.get();
                } catch (SaltaException e) { /* expected */
                }
            }
        });

        assertNotNull(fooProvider.get());
    }

    static class Foo {
    }

    @Test
    public void testMissingBindings() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    getProvider(Runnable.class);
                    bind(Comparator.class);
                    requireBinding(Key.get(new TypeLiteral<Callable<String>>() {
                    }));
                    bind(Date.class).annotatedWith(Names.named("date"));
                }
            });
        } catch (CreationException e) {
            assertEquals(4, e.getErrorMessages().size());
            String segment1 = "No implementation for "
                    + Comparator.class.getName() + " was bound.";
            String segment2 = "No implementation for java.util.Date annotated with @"
                    + Named.class.getName() + "(value=date) was bound.";
            String segment3 = "No implementation for java.lang.Runnable was bound.";
            String segment4 = " No implementation for java.util.concurrent.Callable<java.lang.String> was"
                    + " bound.";
            String atSegment = "at " + getClass().getName();
            String sourceFileName = getDeclaringSourcePart(getClass());
            if (isIncludeStackTraceOff()) {
                assertContains(e.getMessage(), segment1, atSegment,
                        sourceFileName, segment2, atSegment, sourceFileName,
                        segment3, atSegment, sourceFileName, segment4,
                        atSegment, sourceFileName);
            } else {
                assertContains(e.getMessage(), segment3, atSegment,
                        sourceFileName, segment1, atSegment, sourceFileName,
                        segment4, atSegment, sourceFileName, segment2,
                        atSegment, sourceFileName);
            }
        }
    }

    @Test
    public void testMissingDependency() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    bind(NeedsRunnable.class);
                }
            });
        } catch (CreationException e) {
            assertEquals(1, e.getErrorMessages().size());
            assertContains(e.getMessage(),
                    "No implementation for java.lang.Runnable was bound.",
                    "for field at " + NeedsRunnable.class.getName(),
                    ".runnable(BinderTest.java:", "at " + getClass().getName(),
                    getDeclaringSourcePart(getClass()));
        }
    }

    static class NeedsRunnable {
        @Inject
        Runnable runnable;
    }

    @Test
    @Ignore("todo")
    public void testDanglingConstantBinding() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    bindConstant();
                }
            });
            fail();
        } catch (CreationException expected) {
            assertContains(expected.getMessage(),
                    "1) Missing constant value. Please call to(...).", "at "
                            + getClass().getName());
        }
    }

    @Test
    @Ignore("works for salta")
    public void testRecursiveBinding() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    bind(Runnable.class).to(Runnable.class);
                }
            }).getInstance(Runnable.class);
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(), "Detected Dependency Circle:");
        }
    }

    @Test
    public void testBindingNullConstant() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    String none = null;
                    bindConstant().annotatedWith(Names.named("nullOne")).to(
                            none);
                }
            });
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(),
                    "Binding to null instances is not allowed. Use toProvider(Providers.of(null))");
        }
    }

    @Test
    public void testBindingNullInstance() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                public void configure() {
                    String none = null;
                    bind(String.class).annotatedWith(Names.named("nullTwo"))
                            .toInstance(none);
                }
            });
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(),
                    "Binding to null instances is not allowed. Use toProvider(Providers.of(null))");
        }
    }

    @Test
    public void testToStringOnBinderApi() {
        Guice.createInjector(new AbstractModule() {
            @Override
            public void configure() {
                assertEquals("Binder", binder().toString());
                assertEquals("Provider<java.lang.Integer>",
                        getProvider(Integer.class).toString());
                assertEquals(
                        "Provider<Key[type=java.util.List<java.lang.String>, annotation=[none]]>",
                        getProvider(Key.get(new TypeLiteral<List<String>>() {
                        })).toString());

                assertEquals("BindingBuilder<java.lang.Integer>",
                        bind(Integer.class).toString());
                assertEquals("BindingBuilder<java.lang.Integer>",
                        bind(Integer.class).annotatedWith(Names.named("a"))
                                .toString());
                assertEquals("ConstantBindingBuilder", bindConstant()
                        .toString());
                assertEquals("ConstantBindingBuilder", bindConstant()
                        .annotatedWith(Names.named("b")).toString());
            }
        });
    }

    @Test
    public void testNothingIsSerializableInBinderApi() {
        Guice.createInjector(new AbstractModule() {
            @Override
            public void configure() {
                try {
                    assertNotSerializable(binder());
                    assertNotSerializable(getProvider(Integer.class));
                    assertNotSerializable(getProvider(Key
                            .get(new TypeLiteral<List<String>>() {
                            })));
                    assertNotSerializable(bind(Integer.class));
                    assertNotSerializable(bind(Integer.class).annotatedWith(
                            Names.named("a")));
                    assertNotSerializable(bindConstant());
                    assertNotSerializable(bindConstant().annotatedWith(
                            Names.named("b")));
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    /**
     * Although {@code String[].class} isn't equal to {@code new
     * GenericArrayTypeImpl(String.class)}, Guice should treat these two types
     * interchangeably.
     */
    @Test
    public void testArrayTypeCanonicalization() {
        final String[] strings = new String[] { "A" };
        final Integer[] integers = new Integer[] { 1 };

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String[].class).toInstance(strings);
                bind(new TypeLiteral<Integer[]>() {
                }).toInstance(integers);
            }
        });

        assertSame(integers,
                injector.getInstance(Key.get(new TypeLiteral<Integer[]>() {
                })));
        assertSame(integers, injector.getInstance(new Key<Integer[]>() {
        }));
        assertSame(integers, injector.getInstance(Integer[].class));
        assertSame(strings,
                injector.getInstance(Key.get(new TypeLiteral<String[]>() {
                })));
        assertSame(strings, injector.getInstance(new Key<String[]>() {
        }));
        assertSame(strings, injector.getInstance(String[].class));

        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(String[].class).toInstance(new String[] { "A" });
                    bind(new TypeLiteral<String[]>() {
                    }).toInstance(new String[] { "B" });
                }
            });
            fail();
        } catch (SaltaException expected) {
            if (!expected.getMessage().contains("Duplicat"))
                throw expected;
        }

    }

    static class ParentModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new FooModule());
            install(new BarModule());
        }
    }

    static class FooModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new ConstantModule("foo"));
        }
    }

    static class BarModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new ConstantModule("bar"));
        }
    }

    static class ConstantModule extends AbstractModule {
        private final String constant;

        ConstantModule(String constant) {
            this.constant = constant;
        }

        @Override
        protected void configure() {
            bind(String.class).toInstance(constant);
        }
    }

    /**
     * Binding something to two different things should give an error.
     */
    @Test
    public void testSettingBindingTwice() {
        try {
            Guice.createInjector(new ParentModule()).getInstance(String.class);
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(),
                    "Duplicate static binding found");
        }
    }

    /**
     * Binding an @ImplementedBy thing to something else should also fail.
     */
    @Test
    public void testSettingAtImplementedByTwice() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(HasImplementedBy1.class);
                    bind(HasImplementedBy1.class).toInstance(
                            new HasImplementedBy1() {
                            });
                }
            }).getInstance(HasImplementedBy1.class);
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(), "Duplicate static binding");
        }
    }

    /**
     * See issue 614, Problem One https://github.com/google/guice/issues/614
     */
    @Test
    public void testJitDependencyDoesntBlockOtherExplicitBindings() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HasImplementedByThatNeedsAnotherImplementedBy.class);
                bind(HasImplementedBy1.class).toInstance(
                        new HasImplementedBy1() {
                        });
            }
        });
        assertFalse(injector.getInstance(HasImplementedBy1.class) instanceof ImplementsHasImplementedBy1);
    }

    /**
     * See issue 614, Problem Two https://github.com/google/guice/issues/id=614
     */
    @Test
    public void testJitDependencyCanUseExplicitDependencies() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HasImplementedByThatWantsExplicit.class);
                bind(JustAnInterface.class).toInstance(new JustAnInterface() {
                });
            }
        });
    }

    /**
     * Untargetted bindings should follow @ImplementedBy and @ProvidedBy
     * annotations if they exist. Otherwise the class should be constructed
     * directly.
     */
    @Test
    public void testUntargettedBinding() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HasProvidedBy1.class);
                bind(HasImplementedBy1.class);
                bind(HasProvidedBy2.class);
                bind(HasImplementedBy2.class);
                bind(JustAClass.class);
            }
        });

        assertNotNull(injector.getInstance(HasProvidedBy1.class));
        assertNotNull(injector.getInstance(HasImplementedBy1.class));
        assertNotSame(HasProvidedBy2.class,
                injector.getInstance(HasProvidedBy2.class).getClass());
        assertSame(ExtendsHasImplementedBy2.class,
                injector.getInstance(HasImplementedBy2.class).getClass());
        assertSame(JustAClass.class, injector.getInstance(JustAClass.class)
                .getClass());
    }

    @Test
    @Ignore("not applicable")
    public void testPartialInjectorGetInstance() {
        Injector injector = Guice.createInjector();
        try {
            injector.getInstance(MissingParameter.class);
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(),
                    "1) Could not find a suitable constructor in "
                            + NoInjectConstructor.class.getName(), "at "
                            + MissingParameter.class.getName()
                            + ".<init>(BinderTest.java:");
        }
    }

    @Test
    public void testUserReportedError() {
        final Message message = new Message(getClass(), "Whoops!");
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    addError(message);
                }
            });
            fail();
        } catch (SaltaException expected) {
            if (!expected.getMessage().contains("Whoops"))
                throw expected;
        }
    }

    @Test
    public void testBindingToProvider() {
        try {
            Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(new TypeLiteral<Provider<String>>() {
                    }).toInstance(Providers.of("A"));
                }
            });
            fail();
        } catch (SaltaException expected) {
            assertContains(expected.getMessage(),
                    "Binding to core guice framework type is not allowed: Provider.");
        }
    }

    static class OuterCoreModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new InnerCoreModule());
        }
    }

    static class InnerCoreModule extends AbstractModule {
        final Named red = Names.named("red");

        @Override
        protected void configure() {
            bind(AbstractModule.class).annotatedWith(red).toProvider(
                    Providers.<AbstractModule> of(null));
            bind(Binder.class).annotatedWith(red).toProvider(
                    Providers.<Binder> of(null));
            bind(Binding.class).annotatedWith(red).toProvider(
                    Providers.<Binding> of(null));
            bind(Injector.class).annotatedWith(red).toProvider(
                    Providers.<Injector> of(null));
            bind(Key.class).annotatedWith(red).toProvider(
                    Providers.<Key> of(null));
            bind(Module.class).annotatedWith(red).toProvider(
                    Providers.<Module> of(null));
            bind(Provider.class).annotatedWith(red).toProvider(
                    Providers.<Provider> of(null));
            bind(Scope.class).annotatedWith(red).toProvider(
                    Providers.<Scope> of(null));
            bind(Stage.class).annotatedWith(red).toProvider(
                    Providers.<Stage> of(null));
            bind(TypeLiteral.class).annotatedWith(red).toProvider(
                    Providers.<TypeLiteral> of(null));
            bind(new TypeLiteral<Key<String>>() {
            }).toProvider(Providers.<Key<String>> of(null));
        }
    }

    @Test
    @Ignore("todo")
    public void testCannotBindToGuiceTypes() {
        try {
            Guice.createInjector(new OuterCoreModule());
            fail();
        } catch (CreationException expected) {
            assertContains(
                    expected.getMessage(),
                    "Binding to core guice framework type is not allowed: AbstractModule.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Binder.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Binding.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Injector.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Key.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Module.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to Provider is not allowed.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Scope.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Stage.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: TypeLiteral.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class),

                    "Binding to core guice framework type is not allowed: Key.",
                    "at " + InnerCoreModule.class.getName()
                            + getDeclaringSourcePart(getClass()),
                    asModuleChain(OuterCoreModule.class, InnerCoreModule.class));
        }
    }

    static class MissingParameter {
        @Inject
        MissingParameter(NoInjectConstructor noInjectConstructor) {
        }
    }

    static class NoInjectConstructor {
        private NoInjectConstructor() {
        }
    }

    @ProvidedBy(HasProvidedBy1Provider.class)
    interface HasProvidedBy1 {
    }

    static class HasProvidedBy1Provider implements Provider<HasProvidedBy1> {
        @Override
        public HasProvidedBy1 get() {
            return new HasProvidedBy1() {
            };
        }
    }

    @ImplementedBy(ImplementsHasImplementedBy1.class)
    interface HasImplementedBy1 {
    }

    static class ImplementsHasImplementedBy1 implements HasImplementedBy1 {
    }

    @ProvidedBy(HasProvidedBy2Provider.class)
    static class HasProvidedBy2 {
    }

    static class HasProvidedBy2Provider implements Provider<HasProvidedBy2> {
        @Override
        public HasProvidedBy2 get() {
            return new HasProvidedBy2() {
            };
        }
    }

    @ImplementedBy(ExtendsHasImplementedBy2.class)
    static class HasImplementedBy2 {
    }

    static class ExtendsHasImplementedBy2 extends HasImplementedBy2 {
    }

    static class JustAClass {
    }

    @ImplementedBy(ImplementsHasImplementedByThatNeedsAnotherImplementedBy.class)
    static interface HasImplementedByThatNeedsAnotherImplementedBy {
    }

    static class ImplementsHasImplementedByThatNeedsAnotherImplementedBy
            implements HasImplementedByThatNeedsAnotherImplementedBy {
        @Inject
        ImplementsHasImplementedByThatNeedsAnotherImplementedBy(
                HasImplementedBy1 h1n1) {
        }
    }

    @ImplementedBy(ImplementsHasImplementedByThatWantsExplicit.class)
    static interface HasImplementedByThatWantsExplicit {
    }

    static class ImplementsHasImplementedByThatWantsExplicit implements
            HasImplementedByThatWantsExplicit {
        @Inject
        ImplementsHasImplementedByThatWantsExplicit(JustAnInterface jai) {
        }
    }

    static interface JustAnInterface {
    }

    // @Test public void testBindInterfaceWithoutImplementation() {
    // Guice.createInjector(new AbstractModule() {
    // protected void configure() {
    // bind(Runnable.class);
    // }
    // }).getInstance(Runnable.class);
    // }

    enum Roshambo {
        ROCK, SCISSORS, PAPER
    }

    @Test
    public void testInjectRawProvider() {
        try {
            Guice.createInjector().getInstance(Provider.class);
            fail();
        } catch (SaltaException expected) {
            Asserts.assertContains(expected.getMessage(),
                    "Cannot inject a Provider that has no type parameter");
        }
    }
}
