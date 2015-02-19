/**
 * Copyright (C) 2008 Google Inc.
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

package com.google.inject.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Static utility methods for creating and working with instances of
 * {@link Module}.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 * @since 2.0
 */
public final class Modules {
	private Modules() {
	}

	public static final Module EMPTY_MODULE = new EmptyModule();

	private static class EmptyModule implements Module {
		@Override
		public void configure(Binder binder) {
		}
	}

	/**
	 * Returns a new module that installs all of {@code modules}.
	 */
	public static Module combine(Module... modules) {
		return combine(ImmutableSet.copyOf(modules));
	}

	/**
	 * Returns a new module that installs all of {@code modules}.
	 */
	public static Module combine(Iterable<? extends Module> modules) {
		return new CombinedModule(modules);
	}

	private static class CombinedModule implements Module {
		final Set<Module> modulesSet;

		CombinedModule(Iterable<? extends Module> modules) {
			this.modulesSet = ImmutableSet.copyOf(modules);
		}

		@Override
		public void configure(Binder binder) {
			binder = binder.skipSources(getClass());
			for (Module module : modulesSet) {
				binder.install(module);
			}
		}
	}

}
