/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.guice.GuiceInjectorImpl;
import com.github.ruediste.salta.guice.GuiceModule;
import com.github.ruediste.salta.guice.ModuleAdapter;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;

/**
 * The entry point to the Guice framework. Creates {@link Injector}s from
 * {@link Module}s.
 *
 * <p>
 * Guice supports a model of development that draws clear boundaries between
 * APIs, Implementations of these APIs, Modules which configure these
 * implementations, and finally Applications which consist of a collection of
 * Modules. It is the Application, which typically defines your {@code main()}
 * method, that bootstraps the Guice Injector using the {@code Guice} class, as
 * in this example:
 * 
 * <pre>
 * public class FooApplication {
 * 	public static void main(String[] args) {
 *         Injector injector = Guice.createInjector(
 *             new ModuleA(),
 *             new ModuleB(),
 *             . . .
 *             new FooApplicationFlagsModule(args)
 *         );
 * 
 *         // Now just bootstrap the application and you're done
 *         FooStarter starter = injector.getInstance(FooStarter.class);
 *         starter.runApplication();
 *       }
 * }
 * </pre>
 */
public final class Guice {

	private Guice() {
	}

	/**
	 * Creates an injector for the given set of modules. This is equivalent to
	 * calling {@link #createInjector(Stage, Module...)} with Stage.DEVELOPMENT.
	 *
	 * @throws CreationException
	 *             if one or more errors occur during injector construction
	 */
	public static Injector createInjector(Module... modules) {
		return createInjector(Arrays.asList(modules));
	}

	/**
	 * Creates an injector for the given set of modules. This is equivalent to
	 * calling {@link #createInjector(Stage, Iterable)} with Stage.DEVELOPMENT.
	 *
	 * @throws CreationException
	 *             if one or more errors occur during injector creation
	 */
	public static Injector createInjector(Iterable<? extends Module> modules) {
		return createInjector(Stage.DEVELOPMENT, modules);
	}

	/**
	 * Creates an injector for the given set of modules, in a given development
	 * stage.
	 *
	 * @throws CreationException
	 *             if one or more errors occur during injector creation.
	 */
	public static Injector createInjector(Stage stage, Module... modules) {
		return createInjector(stage, Arrays.asList(modules));
	}

	/**
	 * Creates an injector for the given set of modules, in a given development
	 * stage.
	 *
	 * @throws CreationException
	 *             if one or more errors occur during injector construction
	 */
	public static Injector createInjector(Stage stage,
			Iterable<? extends Module> modules) {
		GuiceInjectorConfiguration config = new GuiceInjectorConfiguration(
				stage);
		ArrayList<com.github.ruediste.salta.standard.SaltaModule> wrappedModules = StreamSupport
				.stream(modules.spliterator(), false)
				.map(m -> new ModuleAdapter(m, config))
				.collect(toCollection(ArrayList::new));

		GuiceInjectorImpl injector = new GuiceInjectorImpl();
		wrappedModules.add(new GuiceModule(config, injector));
		Salta.createInjector(stage.getSaltaStage(), wrappedModules);

		// delegate of injector was initialized by the GuiceModule
		return injector;
	}
}
