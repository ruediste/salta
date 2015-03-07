package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FixedMethodInvocationFunctionRecipeTest {

	@Test
	public void checkAssumption() throws Exception {
		assertTrue(void.class.equals(getClass().getMethod("checkAssumption")
				.getReturnType()));
	}
}
