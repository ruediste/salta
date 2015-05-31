/**
 * Copyright (C) 2015 Google Inc.
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

package com.google.inject.spi;

import static com.google.inject.name.Names.named;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;

/** Tests for {@link ModuleAnnotatedMethodScanner} usage. */
public class ModuleAnnotatedMethodScannerTest extends TestCase {

    public void testScanning() throws Exception {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            @Named("foo")
            String foo() {
                return "foo";
            }

            @Provides
            @Named("foo2")
            String foo2() {
                return "foo2";
            }
        };
        Injector injector = Guice.createInjector(module);

        // assert no bindings named "foo" or "foo2" exist -- they were munged.

        Binding<String> fooBinding = injector.getBinding(Key.get(String.class,
                named("foo")));
        Binding<String> foo2Binding = injector.getBinding(Key.get(String.class,
                named("foo2")));
        assertEquals("foo", fooBinding.getProvider().get());
        assertEquals("foo2", foo2Binding.getProvider().get());

        // Validate the provider has a sane toString
        assertEquals(
                "Provider<Key[type=java.lang.String, annotation=@com.google.inject.name.Named(value=foo)]>",
                fooBinding.getProvider().toString());
        assertEquals(
                "Provider<Key[type=java.lang.String, annotation=@com.google.inject.name.Named(value=foo2)]>",
                foo2Binding.getProvider().toString());
    }

    private String methodName(Class<? extends Annotation> annotation,
            String method, Object container) throws Exception {
        return "@" + annotation.getName();
    }

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    private @interface TestProvides {
    }

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    private @interface TestProvides2 {
    }

}
