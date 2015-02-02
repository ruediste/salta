package com.github.ruediste.simpledi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * Provides access to metadata about an injection point.
 * 
 * May represent an injected field or a parameter of a constructor or producer
 * method.
 */
public interface InjectionPoint {
	/**
	 * Obtain an instance of AnnotatedField or AnnotatedParameter, depending
	 * upon whether the injection point is an injected field or a
	 * constructor/method parameter.
	 */
	AnnotatedElement getAnnotated();

	/**
	 * Get the Field object in the case of field injection, the Method object in
	 * the case of method parameter injection or the Constructor object in the
	 * case of constructor parameter injection.
	 * */
	Member getMember();

	/**
	 * Returns the index of this dependency in the method or constructor's
	 * parameter list, or null if this dependency does not belong to a parameter
	 * list.
	 */
	Integer getParameterIndex();
}
