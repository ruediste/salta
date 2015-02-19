package com.github.ruediste.salta.guice;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

public class KeyAdapterTest {

	@Test
	public void typeParameterExtracted() {
		Key<List<Integer>> key = new Key<List<Integer>>() {
		};
		KeyAdapter<List<Integer>> keyAdapter = new KeyAdapter<>(key);
		assertEquals(new TypeToken<List<Integer>>() {
			private static final long serialVersionUID = 1L;
		}, keyAdapter.getType());
		assertEquals(List.class, keyAdapter.getRawType());
	}
}
