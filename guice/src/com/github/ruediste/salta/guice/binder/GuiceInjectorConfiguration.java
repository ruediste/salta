package com.github.ruediste.salta.guice.binder;

import com.google.inject.Stage;

public class GuiceInjectorConfiguration {

	public GuiceInjectorConfiguration(Stage stage) {
		this.stage = stage;
	}

	public final Stage stage;
	public boolean requireExplicitBindings;
	public boolean requireAtInjectOnConstructors;
	public boolean requireExactBindingAnnotations;

}
