package com.github.ruediste.salta.standard.config;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import javax.inject.Singleton;

import org.junit.Test;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.Stage;
import com.google.common.reflect.TypeToken;

public class StandardInjectorConfigurationTest {

    @Singleton
    private static class Base {

    }

    private static class Derived extends Base {

    }

    @Test
    public void testScopeAnnotationNotInherited() {
        StandardInjectorConfiguration config = new StandardInjectorConfiguration(Stage.DEVELOPMENT,
                new CoreInjectorConfiguration());
        Scope singleton = mock(Scope.class);
        Scope def = mock(Scope.class);
        config.scopeAnnotationMap.put(Singleton.class, singleton);
        config.defaultScope = def;

        assertSame(singleton, config.scope.getScope(TypeToken.of(Base.class)));
        assertSame(def, config.scope.getScope(TypeToken.of(Derived.class)));
    }
}
