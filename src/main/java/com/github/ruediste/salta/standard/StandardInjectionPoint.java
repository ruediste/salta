package com.github.ruediste.salta.standard;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import com.github.ruediste.salta.core.InjectionPoint;

public class StandardInjectionPoint implements InjectionPoint {

	final private Member member;
	final private AnnotatedElement annotated;
	final private Integer parameterIndex;

	public StandardInjectionPoint(Member member, AnnotatedElement annotated,
			Integer parameterIndex) {
		this.member = member;
		this.annotated = annotated;
		this.parameterIndex = parameterIndex;
	}

	@Override
	public AnnotatedElement getAnnotated() {
		return annotated;
	}

	@Override
	public Member getMember() {
		return member;
	}

	@Override
	public String toString() {
		return member.toString();
	}

	@Override
	public Integer getParameterIndex() {
		return parameterIndex;
	}
}
