package com.github.ruediste.salta.guice;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.google.inject.Module;
import com.google.inject.Provides;

public class GuiceModuleCheck implements Consumer<Object> {

	@Override
	public void accept(Object module) {
		if (module instanceof ModuleAdapter) {
			check(((ModuleAdapter) module).getDelegate());
		}
	}

	private void check(Module module) {
		Class<?> cls = module.getClass();
		MethodOverrideIndex idx = new MethodOverrideIndex(cls);
		while (!Object.class.equals(cls)) {
			for (Method m : cls.getDeclaredMethods()) {
				if (!idx.wasScanned(m))
					continue;
				Provides provides = m.getAnnotation(Provides.class);
				if (provides == null)
					continue;
				if (idx.isOverridden(m)) {
					throw new SaltaException(
							"Overriding @Provides methods is not allowed:\n "
									+ m
									+ "\nis overridden by \n "
									+ idx.getOverridingMethods(m).stream()
											.map(Objects::toString)
											.collect(joining("\n ")));
				}
			}
			cls = cls.getSuperclass();
		}
	}
}
