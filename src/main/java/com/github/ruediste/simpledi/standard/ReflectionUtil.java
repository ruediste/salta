package com.github.ruediste.simpledi.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionUtil {

	private ReflectionUtil() {
	}

	public static Annotation createAnnotation(Class<?> annotationClass) {
		return (Annotation) Proxy.newProxyInstance(
				annotationClass.getClassLoader(),
				new Class[] { Annotation.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args) {
						return annotationClass; // only getClass() or
												// annotationType()
						// should be called.
					}
				});
	}

}
