package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility methods to determine if a class or class members are is publicly
 * accessible from code loaded using a certain class loader
 */
public class Accessibility {

    private Accessibility() {
    }

    public static boolean isConstructorAccessible(Constructor<?> constructor, ClassLoader cl) {
        return isExecutableAccessible(constructor, cl);
    }

    public static boolean isMethodAccessible(Method m, ClassLoader cl) {
        return isExecutableAccessible(m, cl);
    }

    public static boolean isExecutableAccessible(Executable executable, ClassLoader cl) {
        if (!Modifier.isPublic(executable.getModifiers()))
            return false;
        if (!isClassAccessible(executable.getDeclaringClass(), cl))
            return false;
        for (Class<?> t : executable.getParameterTypes()) {
            if (!isClassAccessible(t, cl))
                return false;
        }
        return true;
    }

    public static boolean isClassAccessible(Class<?> clazz, ClassLoader cl) {
        if (clazz.isPrimitive())
            return true;
        if (clazz.isArray())
            return isClassAccessible(clazz.getComponentType(), cl);
        do {
            if (!Modifier.isPublic(clazz.getModifiers()))
                return false;
            try {
                if (!clazz.equals(cl.loadClass(clazz.getName())))
                    return false;
            } catch (ClassNotFoundException e) {
                // not accessible
                return false;
            }
            clazz = clazz.getEnclosingClass();
        } while (clazz != null);

        return true;
    }

    public static boolean isFieldAccessible(Field field, ClassLoader cl) {
        if (!Modifier.isPublic(field.getModifiers()))
            return false;
        if (!isClassAccessible(field.getDeclaringClass(), cl))
            return false;
        return isClassAccessible(field.getType(), cl);
    }
}
