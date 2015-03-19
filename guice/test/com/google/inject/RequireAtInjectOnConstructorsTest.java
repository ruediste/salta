/*
 * Copyright (C) 2012 Google Inc.
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

/**
 * Tests for {@link Binder#requireAtInjectOnConstructors()}
 * 
 * @author sameb@google.com (Sam Berlin)
 */
public class RequireAtInjectOnConstructorsTest extends TestCase {

	public void testNoCxtors_explicitBinding() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(NoCxtors.class);
					binder().requireAtInjectOnConstructors();
				}
			}).getInstance(NoCxtors.class);
			fail();
		} catch (SaltaException ce) {
			if (!ce.getMessage().contains("Cannot find construction rule"))
				throw ce;
		}
	}

	public void testNoCxtors_jitBinding() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				binder().requireAtInjectOnConstructors();
			}
		});
		try {
			injector.getInstance(NoCxtors.class);
			fail();
		} catch (SaltaException ce) {
			if (!ce.getMessage().contains("inner class"))
				throw ce;
		}
	}

	public void testNoCxtors_implicitBinding() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(Interface.class).to(NoCxtors.class);
					binder().requireAtInjectOnConstructors();
				}
			}).getInstance(Interface.class);
			fail();
		} catch (SaltaException ce) {
			if (!ce.getMessage().contains("Cannot find construction rule"))
				throw ce;
		}
	}

	public void testNoCxtors_inheritedByPrivateModules() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					binder().requireAtInjectOnConstructors();
					install(new AbstractModule() {
						@Override
						protected void configure() {
							bind(NoCxtors.class);
						}
					});
				}
			}).getInstance(NoCxtors.class);
			fail();
		} catch (SaltaException ce) {
			if (!ce.getMessage().contains("Cannot find construction rule"))
				throw ce;
		}
	}

	public void testManyConstructorsButNoneWithAtInject() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(ManyConstructors.class);
					binder().requireAtInjectOnConstructors();
				}
			}).getInstance(ManyConstructors.class);
			fail();
		} catch (SaltaException ce) {
			if (!ce.getMessage().contains("Cannot find construction rule"))
				throw ce;
		}
	}

	public void testRequireAtInjectStillAllowsToConstructorBindings() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				try {
					bind(ManyConstructors.class).toConstructor(
							ManyConstructors.class.getDeclaredConstructor());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				binder().requireAtInjectOnConstructors();
			}
		});
		injector.getInstance(ManyConstructors.class);
	}

	private static interface Interface {
	}

	private static class NoCxtors implements Interface {
	}

	private static class AnotherNoCxtors {
	}

	private static class ManyConstructors {
		@SuppressWarnings("unused")
		ManyConstructors() {
		}

		@SuppressWarnings("unused")
		ManyConstructors(String a) {
		}

		@SuppressWarnings("unused")
		ManyConstructors(int a) {
		}
	}
}
