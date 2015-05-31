package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Base class for static members injectors.
 */
public abstract class StaticMembersInjectorBase {

    protected enum InjectionInstruction {
        NO_INJECT, INJECT, INJECT_OPTIONAL
    }

    /**
     * Determine if a field should be injected. Called for static fields only;
     */
    protected abstract InjectionInstruction shouldInject(Field field);

    /**
     * Determine if a method should be injected. Called for static methods only;
     */
    protected abstract InjectionInstruction shouldInject(Method method);

    /**
     * Injects all
     * {@link StandardInjectorConfiguration#requestedStaticInjections}
     */
    public void injectStaticMembers(StandardInjectorConfiguration config,
            StandardInjector injector) {
        Set<Class<?>> injectedClasses = new HashSet<>();
        for (Class<?> cls : config.requestedStaticInjections) {
            performStaticInjections(cls, injector, injectedClasses);
        }
    }

    private void performStaticInjections(Class<?> cls,
            StandardInjector injector, Set<Class<?>> injectedClasses) {
        if (cls == null)
            return;
        if (injectedClasses.add(cls)) {
            performStaticInjections(cls.getSuperclass(), injector,
                    injectedClasses);

            performStaticInjections(injector.getCoreInjector(), cls);
        }
    }

    private void performStaticInjections(CoreInjector injector, Class<?> cls) {
        // inject fields
        for (Field f : cls.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()))
                continue;
            InjectionInstruction injectionInstruction = shouldInject(f);
            if (injectionInstruction == InjectionInstruction.NO_INJECT)
                continue;
            InjectionPoint<?> d = new InjectionPoint<>(TypeToken.of(f
                    .getGenericType()), f, f, null);
            f.setAccessible(true);
            Optional<CompiledSupplier> instance = injector
                    .tryGetCompiledRecipe(d);
            if (instance.isPresent()
                    || injectionInstruction != InjectionInstruction.INJECT_OPTIONAL)
                try {
                    f.set(null, instance.get().getNoThrow());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new SaltaException("Error while setting static " + f,
                            e);
                }
        }

        // inject methods
        methodLoop: for (Method m : cls.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers()))
                continue;
            InjectionInstruction injectionInstruction = shouldInject(m);
            if (injectionInstruction == InjectionInstruction.NO_INJECT)
                continue;
            ArrayList<Object> args = new ArrayList<>();
            Parameter[] parameters = m.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter p = parameters[i];
                CoreDependencyKey<?> d = new InjectionPoint<>(TypeToken.of(p
                        .getParameterizedType()), m, p, i);
                if (injectionInstruction == InjectionInstruction.INJECT_OPTIONAL) {
                    Optional<CompiledSupplier> tmp = injector
                            .tryGetCompiledRecipe(d);
                    if (!tmp.isPresent())
                        continue methodLoop;
                    args.add(tmp.get().getNoThrow());
                } else
                    args.add(injector.getInstance(d));
            }

            m.setAccessible(true);
            try {
                m.invoke(null, args.toArray());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new SaltaException("Error while setting static " + m, e);
            } catch (InvocationTargetException e) {
                throw new SaltaException("Error while setting static " + m,
                        e.getCause());
            }

        }
    }

}