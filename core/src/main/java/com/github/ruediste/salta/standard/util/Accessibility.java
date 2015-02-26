package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility methods to determine if a class is publically accessible
 */
public class Accessibility {

	private Accessibility() {
	}

	public static boolean isConstructorPublic(Constructor<?> constructor) {
		return isExecutablePublic(constructor);
	}

	public static boolean isMethodPublic(Method m) {
		return isExecutablePublic(m);
	}

	public static boolean isExecutablePublic(Executable executable) {
		if (!Modifier.isPublic(executable.getModifiers()))
			return false;
		if (!isClassPublic(executable.getDeclaringClass()))
			return false;
		for (Class<?> t : executable.getParameterTypes()) {
			if (!isClassPublic(t))
				return false;
		}
		return true;
	}

	public static boolean isClassPublic(Class<?> clazz) {
		do {
			if (!Modifier.isPublic(clazz.getModifiers()))
				return false;
			clazz = clazz.getEnclosingClass();
		} while (clazz != null);

		return true;
	}

	public static boolean isFieldPublic(Field field) {
		if (!Modifier.isPublic(field.getModifiers()))
			return false;
		return isClassPublic(field.getType());
	}
}
