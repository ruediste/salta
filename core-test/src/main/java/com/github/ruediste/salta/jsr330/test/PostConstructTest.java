package com.github.ruediste.salta.jsr330.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.PostConstruct;
import com.github.ruediste.salta.jsr330.Salta;

public class PostConstructTest {

	private static class TestClass {
		int initializeCount;

		@PostConstruct
		void init() {
			initializeCount++;
		}
	}

	private static class TestClassDerived extends TestClass {

		@Override
		@PostConstruct
		void init() {
			initializeCount++;
		}
	}

	@Test
	public void testPostConstructCalled() {
		assertEquals("initialization called once", 1,
				Salta.createInjector().getInstance(TestClass.class).initializeCount);
	}

	@Test
	public void testPostConstructCalledOnDerived() {
		assertEquals("initialization called once", 1,
				Salta.createInjector().getInstance(TestClassDerived.class).initializeCount);
	}
}
