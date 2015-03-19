package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

/**
 * Indexes the methods of a class and all it's superclasses and aswers for each
 * method declared in the class hierarchy if the method is overridden (within
 * the hierarchy)
 */
public class MethodOverrideIndex {

	private final HashMap<Signature, Set<Method>> leafMethods = new HashMap<>();
	private final HashMap<Signature, Set<Method>> methodsBySignature = new HashMap<>();
	private final HashSet<Method> scannedMethods = new HashSet<>();

	private final List<TypeToken<?>> ancestors;

	public MethodOverrideIndex(Class<?> leafClass) {
		this(TypeToken.of(leafClass));
	}

	public MethodOverrideIndex(TypeToken<?> leafType) {
		ancestors = Collections.unmodifiableList(Lists.reverse(new ArrayList<>(
				leafType.getTypes())));

		// we visit parent classes always before child classes
		for (TypeToken<?> type : ancestors) {
			for (Method m : type.getRawType().getDeclaredMethods()) {
				if (Modifier.isStatic(m.getModifiers())
				// || m.isBridge()
				// || m.isSynthetic()
				) {
					continue;
				}

				scannedMethods.add(m);

				Signature signature = new Signature(m);

				// record the methods
				methodsBySignature.computeIfAbsent(signature,
						s -> new HashSet<>()).add(m);

				// get current leaf methods for the signature
				Set<Method> oldLeaves = leafMethods.get(signature);
				if (oldLeaves == null) {
					oldLeaves = new HashSet<>();
				}

				// filter those methods out which are being overridden by this
				// method
				Set<Method> newLeaves = new HashSet<>();
				for (Method ancestorMethod : oldLeaves) {
					if (!doesOverride(m, ancestorMethod))
						newLeaves.add(ancestorMethod);

				}

				// the current method is of course a leaf
				newLeaves.add(m);

				leafMethods.put(signature, newLeaves);
			}
		}

	}

	private boolean doesOverride(Method m, Method ancestorMethod) {
		if (Modifier.isFinal(ancestorMethod.getModifiers()))
			return false;
		if (Modifier.isPublic(ancestorMethod.getModifiers()))
			return true;
		if (Modifier.isProtected(ancestorMethod.getModifiers()))
			return true;
		if (Modifier.isPrivate(ancestorMethod.getModifiers()))
			return false;

		// ancestorMethod must be package visible
		String ancestorPackage = ancestorMethod.getDeclaringClass()
				.getPackage().getName();
		String mPackage = m.getDeclaringClass().getPackage().getName();
		if (Objects.equals(ancestorPackage, mPackage))
			return true;
		return false;
	}

	public HashSet<Method> getOverridingMethods(Method ancestor) {
		HashSet<Method> result = new HashSet<>();
		Set<Method> set = methodsBySignature.get(new Signature(ancestor));
		if (set == null)
			throw new RuntimeException("Method " + ancestor
					+ " was not scanned by this override index");
		for (Method m : set) {
			if (ancestor.equals(m))
				continue;
			if (doesOverride(m, ancestor))
				result.add(m);
		}
		return result;

	}

	public boolean wasScanned(Method m) {
		return scannedMethods.contains(m);
	}

	public boolean isOverridden(Method m) {
		Set<Method> leaves = leafMethods.get(new Signature(m));
		if (leaves == null)
			throw new RuntimeException("Method " + m
					+ " was not scanned by this override index");
		return !leaves.contains(m);
	}

	public List<TypeToken<?>> getAncestors() {
		return ancestors;
	}

	private static class Signature {

		private String name;
		private Class<?>[] parameterTypes;

		public Signature(Method m) {
			name = m.getName();
			parameterTypes = m.getParameterTypes();
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, Arrays.hashCode(parameterTypes));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (!getClass().equals(obj.getClass()))
				return false;
			Signature other = (Signature) obj;
			return Objects.equals(name, other.name)
					&& Arrays.equals(parameterTypes, other.parameterTypes);
		}

		@Override
		public String toString() {
			return name + "(" + parameterTypes + ")";
		}
	}
}
