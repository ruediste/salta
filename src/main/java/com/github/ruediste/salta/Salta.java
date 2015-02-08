package com.github.ruediste.salta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Module;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.StandardModule;
import com.github.ruediste.salta.standard.binder.Binder;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * Entry point to the injection framework
 */
public class Salta {

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
				Stage.DEVELOPMENT, new CoreInjectorConfiguration());
		StandardInjector injector = new StandardInjector();
		Binder binder = new Binder(config, injector);
		for (Module module : modules) {
			module.configure(binder);
		}

		injector.initialize(config);

		return injector;
	}
}
