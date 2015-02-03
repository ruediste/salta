package com.github.ruediste.simpledi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.internal.InjectorImpl;
import com.github.ruediste.simpledi.internal.defaultModule.DefaultModule;

/**
 * Entry point to the injection framework
 */
public class SimpleDi {

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(Module... modules) {
		return createInjector(Arrays.asList(modules));
	}

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(List<Module> modules) {
		List<Module> realModules = new ArrayList<>(modules);
		realModules.add(new DefaultModule());
		return createRawInjector(realModules);
	}

	/**
	 * Create an {@link Injector} without the {@link DefaultModule}
	 */
	public static Injector createRawInjector(Module... modules) {
		return createRawInjector(Arrays.asList(modules));
	}

	/**
	 * Create an {@link Injector} without the {@link DefaultModule}
	 */
	public static Injector createRawInjector(List<Module> modules) {
		return new InjectorImpl(Modules.getRules(modules));
	}
}
