package com.github.ruediste.salta.standard.recipe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import com.github.ruediste.salta.core.SaltaException;

public class UnrestrictedLookupHolder {
	public static Lookup lookup;

	static {
		try {
			Field f = MethodHandles.Lookup.class
					.getDeclaredField("IMPL_LOOKUP");
			f.setAccessible(true);
			lookup = (Lookup) f.get(null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new SaltaException("Error while retrieving unbound lookup");
		}
	}
}
