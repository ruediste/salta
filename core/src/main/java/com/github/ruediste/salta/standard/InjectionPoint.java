package com.github.ruediste.salta.standard;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.Objects;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.google.common.reflect.TypeToken;

/**
 * Provides access to metadata about an injection point.
 * 
 * May represent an injected field or a parameter of a constructor or producer
 * method.
 */
public class InjectionPoint<T> extends CoreDependencyKey<T> {
	private final TypeToken<T> type;
	private final Member member;
	private final AnnotatedElement annotated;
	private final Integer parameterIndex;
	private Class<T> rawType;
	private final int hashCode;

	@SuppressWarnings("unchecked")
	public InjectionPoint(TypeToken<T> type, Member member,
			AnnotatedElement annotated, Integer parameterIndex) {
		this.type = type;
		this.rawType = (Class<T>) type.getRawType();
		this.member = member;
		this.annotated = annotated;
		this.parameterIndex = parameterIndex;
		this.hashCode = Objects.hash(type, member, annotated, parameterIndex);
	}

	/**
	 * Obtain an instance of AnnotatedField or AnnotatedParameter, depending
	 * upon whether the injection point is an injected field or a
	 * constructor/method parameter.
	 */
	@Override
	public AnnotatedElement getAnnotatedElement() {
		return annotated;
	}

	/**
	 * Get the Field object in the case of field injection, the Method object in
	 * the case of method parameter injection or the Constructor object in the
	 * case of constructor parameter injection.
	 * */
	public Member getMember() {
		return member;
	}

	/**
	 * Returns the index of this dependency in the method or constructor's
	 * parameter list, or null if this dependency does not belong to a parameter
	 * list.
	 */
	public Integer getParameterIndex() {
		return parameterIndex;
	}

	@Override
	public String toString() {
		if (member instanceof Executable && annotated instanceof Parameter) {
			return "parameter " + annotated + " of " + member;
		}
		return member.toString();
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!Objects.equals(getClass(), obj.getClass()))
			return false;
		InjectionPoint<?> other = (InjectionPoint<?>) obj;
		return Objects.equals(type, other.type)
				&& Objects.equals(member, other.member)
				&& Objects.equals(annotated, other.annotated)
				&& Objects.equals(parameterIndex, other.parameterIndex);
	}

	@Override
	public Class<T> getRawType() {
		return rawType;
	}
}
