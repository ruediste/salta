package com.github.ruediste.salta;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Module;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.Binder;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * Entry point to the injection framework
 */
public class Salta {

	public static Injector createInjector(Module... modules) {
		return createInjector(Stage.DEVELOPMENT, modules);
	}

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(Stage stage, Module... modules) {
		return createInjector(stage, Arrays.asList(modules));
	}

	public static Injector createInjector(List<Module> modules) {
		return createInjector(Stage.DEVELOPMENT, modules);
	}

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(Stage stage, List<Module> modules) {
		StandardInjectorConfiguration config = new StandardInjectorConfiguration(
				stage, new CoreInjectorConfiguration());
		StandardInjector injector = new StandardInjector();
		Binder binder = new Binder(config, injector);
		for (Module module : modules) {
			module.configure(binder);
		}

		binder.close();

		if (!config.errorMessages.isEmpty()) {
			throw new SaltaException("There were Errors:\n"
					+ config.errorMessages.stream()
							.map(msg -> msg.getMessage())
							.collect(joining("\n")));
		}
		injector.initialize(config);

		return injector;
	}

}
