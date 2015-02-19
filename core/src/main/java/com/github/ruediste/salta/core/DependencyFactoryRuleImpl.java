package com.github.ruediste.salta.core;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.matchers.Matcher;

public class DependencyFactoryRuleImpl implements DependencyFactoryRule {

	private Function<CoreDependencyKey<?>, Supplier<Object>> supplierFactory;
	private Matcher<? super CoreDependencyKey<?>> matcher;

	public DependencyFactoryRuleImpl(
			Matcher<? super CoreDependencyKey<?>> matcher,
			Function<CoreDependencyKey<?>, Supplier<Object>> supplierFactory) {
		this.matcher = matcher;
		this.supplierFactory = supplierFactory;
	}

	@Override
	public CreationRecipe apply(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		if (matcher.matches(key))
			return new CreationRecipeImpl(supplierFactory.apply(key));
		else
			return null;
	}

}
