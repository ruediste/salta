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

package com.google.inject;

import junit.framework.TestCase;

/**
 * @author jessewilson@google.com (Jesse Wilson)
 */
public class EagerSingletonTest extends TestCase {

    @Override
    public void setUp() {
        A.instanceCount = 0;
        B.instanceCount = 0;
        C.instanceCount = 0;
    }

    public void testJustInTimeSingletonsAreNotEager() {
        Injector injector = Guice.createInjector(Stage.PRODUCTION);
        injector.getProvider(B.class);
        assertEquals(0, B.instanceCount);
    }

    @Singleton
    static class A {
        static int instanceCount = 0;
        int instanceId = instanceCount++;

        @Inject
        A(Injector injector) {
            injector.getProvider(B.class);
        }
    }

    @Singleton
    static class B {
        static int instanceCount = 0;
        int instanceId = instanceCount++;
    }

    @Singleton
    static class C implements D {
        static int instanceCount = 0;
        int instanceId = instanceCount++;
    }

    private static interface D {
    }
}
