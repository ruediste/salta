package com.github.ruediste.salta;

import java.util.Arrays;
import java.util.List;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.SaltaModule;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.Binder;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * Entry point to the injection framework
 */
public class Salta {

	public static Injector createInjector(SaltaModule... modules) {
		return createInjector(Stage.DEVELOPMENT, modules);
	}

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(Stage stage, SaltaModule... modules) {
		return createInjector(stage, Arrays.asList(modules));
	}

	public static Injector createInjector(List<SaltaModule> modules) {
		return createInjector(Stage.DEVELOPMENT, modules);
	}

	/**
	 * Creates an injector for the given set of modules.
	 */
	public static Injector createInjector(Stage stage, List<SaltaModule> modules) {
		StandardInjectorConfiguration config = new StandardInjectorConfiguration(
				stage, new CoreInjectorConfiguration());
		StandardInjector injector = new StandardInjector();
		Binder binder = new Binder(config, injector);
		for (SaltaModule module : modules) {
			binder.install(module);
		}

		binder.close();

		injector.initialize(config);

		return injector;
	}

}
