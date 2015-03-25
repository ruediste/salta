package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

/**
 * A set of {@link StaticBinding}s, ready to be used to create instances
 */
public class StaticBindingSet implements CreationRule {
	private HashMap<TypeToken<?>, List<StaticBinding>> staticBindingMap = new HashMap<>();
	private ArrayList<StaticBinding> nonTypeSpecificStaticBindings = new ArrayList<>();

	public StaticBindingSet(Iterable<StaticBinding> staticBindings) {
		// check uniqueness of static bindings
		{
			HashMap<Matcher<?>, StaticBinding> map = new HashMap<>();
			for (StaticBinding b : staticBindings) {
				StaticBinding existing = map.put(b.getMatcher(), b);
				if (existing != null) {
					throw new SaltaException("Duplicate static binding found\n"
							+ b + "\n" + existing);
				}
			}
		}

		// initialize static binding map
		for (StaticBinding binding : staticBindings) {
			Set<TypeToken<?>> possibleTypes = binding.getPossibleTypes();
			if (possibleTypes == null || possibleTypes.isEmpty())
				nonTypeSpecificStaticBindings.add(binding);
			else {
				for (TypeToken<?> t : possibleTypes) {
					List<StaticBinding> list = staticBindingMap.get(t);
					if (list == null) {
						list = new ArrayList<>();
						staticBindingMap.put(t, list);
					}
					list.add(binding);
				}
			}
		}
	}

	public StaticBinding getBinding(CoreDependencyKey<?> key) {
		StaticBinding binding = null;
		List<StaticBinding> typeSpecificBindings = staticBindingMap.get(key
				.getType());
		if (typeSpecificBindings == null)
			typeSpecificBindings = Collections.emptyList();

		for (StaticBinding b : Iterables.concat(nonTypeSpecificStaticBindings,
				typeSpecificBindings)) {
			if (b.getMatcher().matches(key)) {
				if (binding != null)
					throw new SaltaException(
							"multiple bindings match dependency " + key
									+ "\n * " + binding + "\n * " + b);
				binding = b;
			}
		}

		return binding;
	}

	@Override
	public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
			CoreDependencyKey<?> key) {
		StaticBinding binding = getBinding(key);
		if (binding != null) {
			return Optional.of(ctx -> binding.getScope().createRecipe(ctx,
					binding, key.getType()));
		}
		return Optional.empty();
	}

}
