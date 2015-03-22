package com.github.ruediste.salta.jsr330;

import java.util.Arrays;
import java.util.List;

import com.github.ruediste.salta.jsr330.binder.Binder;
import com.github.ruediste.salta.standard.Stage;

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
		JSR330InjectorConfiguration config = new JSR330InjectorConfiguration(
				stage);
		InjectorImpl injector = new InjectorImpl(config);

		Binder binder = new Binder(config, injector);

		for (SaltaModule module : modules) {
			binder.install(module);
		}

		binder.close();

		config.postProcessModules();

		injector.initialize();

		return injector;
	}

}
