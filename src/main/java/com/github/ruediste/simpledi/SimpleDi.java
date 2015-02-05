package com.github.ruediste.simpledi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.InjectorConfiguration;
import com.github.ruediste.simpledi.core.Stage;
import com.github.ruediste.simpledi.core.internal.InjectorImpl;
import com.github.ruediste.simpledi.standard.Module;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardModule;
import com.github.ruediste.simpledi.standard.binder.Binder;

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
		realModules.add(new StandardModule());
		return createRawInjector(realModules);
	}

	/**
	 * Create an {@link Injector} without the {@link StandardModule}
	 */
	public static Injector createRawInjector(Module... modules) {
		return createRawInjector(Arrays.asList(modules));
	}

	/**
	 * Create an {@link Injector} without the {@link StandardModule}
	 */
	public static Injector createRawInjector(List<Module> modules) {
		StandardInjectorConfiguration config = new StandardInjectorConfiguration(
				new InjectorConfiguration(Stage.DEVELOPMENT));
		Binder binder = new Binder(config);
		for (Module module : modules) {
			module.configure(binder);
		}
		return new InjectorImpl(config.config);
	}
}
