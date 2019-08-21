package com.github.ruediste.salta.jsr330.test.wikiChecks;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Salta;

public class InjectionPointSpecificInjectionsTest {
	private static class A {
		@Inject
		Logger log;
	}

	@Test
	public void test() {
		A a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				bindCreationRule(new CreationRuleImpl(CoreDependencyKey.rawTypeMatcher(Logger.class),
						key -> () -> Logger.getLogger(key.getRawType().getName())));
			}
		}).getInstance(A.class);
		assertNotNull(a.log);
	}
}
