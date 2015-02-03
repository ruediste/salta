package com.github.ruediste.simpledi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import com.github.ruediste.simpledi.core.InjectionPoint;

public class DefaultInjectionPoint implements InjectionPoint {

	final private Member member;
	final private AnnotatedElement annotated;

	public DefaultInjectionPoint(Member member, AnnotatedElement annotated) {
		this.member = member;
		this.annotated = annotated;
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
}
