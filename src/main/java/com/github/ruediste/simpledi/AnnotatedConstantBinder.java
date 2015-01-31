package com.github.ruediste.simpledi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class AnnotatedConstantBinder {

	private List<Rule> rules;

	public AnnotatedConstantBinder(List<Rule> rules) {
		this.rules = rules;
	}

	public ConstantBinder named(String name) {
		return annotatedWith(ReflectionUtil.createNamed(name));
	}

	public ConstantBinder annotatedWith(
			Class<? extends Annotation> annotationType) {
		return annotatedWith(ReflectionUtil.createAnnotation(annotationType));
	}

	public ConstantBinder annotatedWith(Annotation... annotations) {
		return new ConstantBinder(Arrays.asList(annotations), rules);
	}
}
