package com.github.ruediste.simpledi;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.reflect.TypeToken;

/**
 * The type and required qualifiers to lookup an instance.
 */
public class Key<T> {
	public final TypeToken<T> type;
	public final List<Annotation> requiredQualifiers;

	public Key(Class<T> type) {
		this(TypeToken.of(type));
	}

	public Key(TypeToken<T> type) {
		this.type = type;
		requiredQualifiers = Collections.emptyList();
	}

	public Key(Class<T> type, Annotation... requiredQualifiers) {
		this(type, Arrays.asList(requiredQualifiers));
	}

	public Key(Class<T> type, List<Annotation> requiredQualifiers) {
		this(TypeToken.of(type), requiredQualifiers);
	}

	public Key(TypeToken<T> type, List<Annotation> requiredQualifiers) {
		this.type = type;
		this.requiredQualifiers = requiredQualifiers;
	}

	@Override
	public String toString() {
		String qualifiers = "";
		if (requiredQualifiers.size() > 0)
			qualifiers = " qualifiers: "
					+ requiredQualifiers.stream().map(Object::toString)
							.collect(joining(","));
		return "(type " + type + qualifiers + ")";
	}
}
