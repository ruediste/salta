/**
 * Copyright (C) 2011 Google Inc.
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

import static com.google.inject.Asserts.assertContains;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.github.ruediste.salta.core.SaltaException;
import com.google.common.reflect.TypeToken;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.util.Providers;

/**
 * Tests for {@link Binder#bindListener(Matcher, ProvisionListener...)}
 * 
 * @author sameb@google.com (Sam Berlin)
 */
// TODO(sameb): Add some tests for private modules & child injectors.
public class ProvisionListenerTest extends TestCase {

	public void testExceptionInListenerBeforeProvisioning() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new FailBeforeProvision());
			}
		});
		try {
			injector.getInstance(Foo.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("boo"))
				throw e;
		}
	}

	public void testExceptionInListenerAfterProvisioning() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new FailAfterProvision());
			}
		});
		try {
			injector.getInstance(Foo.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("boo"))
				throw e;
		}
	}

	public void testExceptionInProvisionExplicitlyCalled() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new JustProvision());
			}
		});
		try {
			injector.getInstance(FooBomb.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("Retry, Abort, Fail"))
				throw e;
		}
	}

	public void testExceptionInProvisionAutomaticallyCalled() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new NoProvision());
			}
		});
		try {
			injector.getInstance(FooBomb.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("Retry, Abort, Fail"))
				throw e;
		}
	}

	public void testExceptionInFieldProvision() throws Exception {
		final CountAndCaptureExceptionListener listener = new CountAndCaptureExceptionListener();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(new AbstractMatcher<TypeToken<?>>() {
					@Override
					public boolean matches(TypeToken<?> binding) {
						return binding.getRawType().equals(
								DependsOnFooBombInField.class);
					}
				}, listener);
			}
		});
		assertEquals(0, listener.beforeProvision);
		try {
			injector.getInstance(DependsOnFooBombInField.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("Retry, Abort, Fail"))
				throw e;
		}
		assertEquals(2, listener.beforeProvision);
		assertEquals("Retry, Abort, Fail", listener.capture.get().getMessage());
		assertEquals(0, listener.afterProvision);
	}

	public void testExceptionInCxtorProvision() throws Exception {
		final CountAndCaptureExceptionListener listener = new CountAndCaptureExceptionListener();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(new AbstractMatcher<TypeToken<?>>() {
					@Override
					public boolean matches(TypeToken<?> type) {
						return type.getRawType().equals(
								DependsOnFooBombInCxtor.class);
					}
				}, listener);
			}
		});
		assertEquals(0, listener.beforeProvision);
		try {
			injector.getInstance(DependsOnFooBombInCxtor.class);
			fail();
		} catch (SaltaException expected) {
			if (!expected.getMessage().contains("Retry, Abort, Fail"))
				throw expected;
		}
		assertEquals(2, listener.beforeProvision);
		assertEquals("Retry, Abort, Fail", listener.capture.get().getMessage());
		assertEquals(0, listener.afterProvision);
	}

	public void testListenerCallsProvisionTwice() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new ProvisionTwice());
			}
		});
		try {
			injector.getInstance(Foo.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("provision() already called"))
				throw e;
		}
	}

	public void testCachedInScopePreventsProvisionNotify() {
		final Counter count1 = new Counter();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), count1);
				bind(Foo.class).in(Scopes.SINGLETON);
			}
		});
		Foo foo = injector.getInstance(Foo.class);
		assertNotNull(foo);
		assertEquals(2, count1.count);

		// not notified the second time because nothing is provisioned
		// (it's cached in the scope)
		count1.count = 0;
		assertSame(foo, injector.getInstance(Foo.class));
		assertEquals(0, count1.count);
	}

	public void testCombineAllBindListenerCalls() {
		final Counter count1 = new Counter();
		final Counter count2 = new Counter();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), count1);
				bindListener(Matchers.any(), count2);
			}
		});
		assertNotNull(injector.getInstance(Foo.class));
		assertEquals(2, count1.count);
		assertEquals(2, count2.count);
	}

	public void testNotifyEarlyListenersIfFailBeforeProvision() {
		final Counter count1 = new Counter();
		final Counter count2 = new Counter();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), count1, new FailBeforeProvision(),
						count2);
			}
		});
		try {
			injector.getInstance(Foo.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("boo"))
				throw e;
		}
	}

	public void testNotifyLaterListenersIfFailAfterProvision() {
		final Counter count1 = new Counter();
		final Counter count2 = new Counter();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), count1, new FailAfterProvision(),
						count2);
			}
		});
		try {
			injector.getInstance(Foo.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("boo"))
				throw e;
		}
	}

	public void testCallingBindingDotGetProviderDotGet() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new ProvisionListener() {
					@Override
					public <T> void onProvision(ProvisionInvocation<T> provision) {
						provision.getBinding().getProvider().get(); // AGH!
					}
				});
			}
		});

		try {
			injector.getInstance(Sole.class);
			fail();
		} catch (Exception expected) {
			// We don't really care what kind of error you get, we only care you
			// get an error.
		}

		try {
			injector.getInstance(Many.class);
			fail();
		} catch (Exception expected) {
			// We don't really care what kind of error you get, we only care you
			// get an error.
		}
	}

	interface Interface {
	}

	class Implementation implements Interface {
	}

	@Singleton
	static class Sole {
	}

	static class Many {
	}

	@ImplementedBy(Foo.class)
	static interface JitFoo {
	}

	@ProvidedBy(JitFoo2P.class)
	static class JitFoo2 {
	}

	static interface LinkedFoo {
	}

	static class Foo implements JitFoo, LinkedFoo {
	}

	static class FooP implements Provider<Foo> {
		@Override
		public Foo get() {
			return new Foo();
		}
	}

	static class JitFoo2P implements Provider<JitFoo2> {
		@Override
		public JitFoo2 get() {
			return new JitFoo2();
		}
	}

	static class FooBomb {
		FooBomb() {
			throw new RuntimeException("Retry, Abort, Fail");
		}
	}

	static class DependsOnFooBombInField {
		@Inject
		FooBomb fooBomb;
	}

	static class DependsOnFooBombInCxtor {
		@Inject
		DependsOnFooBombInCxtor(FooBomb fooBomb) {
		}
	}

	private static class Counter implements ProvisionListener {
		int count = 0;

		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			count++;
		}
	}

	private static class CountAndCaptureExceptionListener implements
			ProvisionListener {
		int beforeProvision = 0;
		int afterProvision = 0;
		AtomicReference<RuntimeException> capture = new AtomicReference<RuntimeException>();

		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			beforeProvision++;
			try {
				provision.provision();
			} catch (RuntimeException re) {
				capture.set(re);
				throw re;
			}
			afterProvision++;
		}
	}

	private static class FailBeforeProvision implements ProvisionListener {
		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			throw new RuntimeException("boo");
		}
	}

	private static class FailAfterProvision implements ProvisionListener {
		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			provision.provision();
			throw new RuntimeException("boo");
		}
	}

	private static class JustProvision implements ProvisionListener {
		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			provision.provision();
		}
	}

	private static class NoProvision implements ProvisionListener {
		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
		}
	}

	private static class ProvisionTwice implements ProvisionListener {
		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			provision.provision();
			provision.provision();
		}
	}

	private static Matcher<Binding<?>> keyMatcher(final Class<?> clazz) {
		return new AbstractMatcher<Binding<?>>() {
			@Override
			public boolean matches(Binding<?> t) {
				return t.getKey().equals(Key.get(clazz));
			}
		};
	}

	public void testModuleRequestInjection() {
		final AtomicBoolean notified = new AtomicBoolean();
		Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				requestInjection(new Object() {
					@Inject
					Foo foo;
				});
				bindListener(Matchers.any(), new SpecialChecker(Foo.class,
						notified));
			}
		});
		assertTrue(notified.get());
	}

	public void testToProviderInstance() {
		final AtomicBoolean notified = new AtomicBoolean();
		Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Object.class).toProvider(new Provider<Object>() {
					@Inject
					Foo foo;

					@Override
					public Object get() {
						return null;
					}
				});
				bindListener(Matchers.any(), new SpecialChecker(Foo.class,
						notified));
			}
		}).getInstance(Object.class);
		assertTrue(notified.get());
	}

	public void testInjectorInjectMembers() {
		final Object object = new Object() {
			@Inject
			Foo foo;
		};
		final AtomicBoolean notified = new AtomicBoolean();
		Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new SpecialChecker(Foo.class,
						notified));
			}
		}).injectMembers(object);
		assertTrue(notified.get());
	}

	private static class SpecialChecker implements ProvisionListener {
		private final Class<?> notifyType;
		private final AtomicBoolean notified;

		public SpecialChecker(Class<?> notifyType, AtomicBoolean notified) {
			this.notifyType = notifyType;
			this.notified = notified;
		}

		@Override
		public <T> void onProvision(ProvisionInvocation<T> provision) {
			notified.set(true);
			assertEquals(notifyType, provision.getBinding().getKey()
					.getRawType());
		}
	}

	private static class Instance {
		@Inject
		A a;
	}

	private static class A {
		@Inject
		A(B b) {
		}
	}

	private interface B {
	}

	private static class BImpl implements B {
		@Inject
		void inject(C c) {
		}
	}

	private interface C {
	}

	private interface D {
	}

	private static class DP implements Provider<D> {
		@Inject
		Provider<E> ep;

		@Override
		public D get() {
			ep.get();
			return new D() {
			};
		}
	}

	private static class E {
		@SuppressWarnings("unused")
		@Inject
		F f;
	}

	private static class F {
	}

	public void testBindToInjectorWithListeningGivesSaneException() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bindListener(Matchers.any(), new Counter());
					bind(Injector.class).toProvider(
							Providers.<Injector> of(null));
				}
			});
			fail();
		} catch (SaltaException ce) {
			assertContains(ce.getMessage(),
					"Binding to core guice framework type is not allowed: Injector.");
		}
	}

	public void testProvisionIsNotifiedAfterContextsClear() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new ProvisionListener() {
					@Override
					public <T> void onProvision(ProvisionInvocation<T> provision) {
						Object provisioned = provision.provision();
						if (provisioned instanceof X) {
							((X) provisioned).init();
						} else if (provisioned instanceof Y) {
							X.createY = false;
							((Y) provisioned).init();
						}
					}
				});
			}
		});

		X.createY = true;
		X x = injector.getInstance(X.class);
		assertNotSame(x, x.y.x);
		assertFalse("x.ID: " + x.ID + ", x.y.x.iD: " + x.y.x.ID,
				x.ID == x.y.x.ID);
	}

	private static class X {
		final static AtomicInteger COUNTER = new AtomicInteger();
		static boolean createY;

		final int ID = COUNTER.getAndIncrement();
		final Provider<Y> yProvider;
		Y y;

		@Inject
		X(Provider<Y> yProvider) {
			this.yProvider = yProvider;
		}

		void init() {
			if (createY) {
				this.y = yProvider.get();
			}
		}
	}

	private static class Y {
		final Provider<X> xProvider;
		X x;

		@Inject
		Y(Provider<X> xProvider) {
			this.xProvider = xProvider;
		}

		void init() {
			this.x = xProvider.get();
		}
	}

}
