package com.github.ruediste.salta.jsr330;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

public class JSR330InjectorConfiguration {

	public final StandardInjectorConfiguration config;

	public JSR330InjectorConfiguration(Stage stage) {
		this(new StandardInjectorConfiguration(stage));
	}

	public JSR330InjectorConfiguration(StandardInjectorConfiguration config) {
		this.config = config;
	}

	/**
	 * List of modules that were used to create this configuration. Used to post
	 * process the modules after the first configuration
	 */
	public List<SaltaModule> modules = new ArrayList<>();

	public List<Consumer<SaltaModule>> modulePostProcessors = new ArrayList<>();

	public void postProcessModules() {
		for (SaltaModule module : modules) {
			modulePostProcessors.stream().forEach(x -> x.accept(module));
		}
	}
}
