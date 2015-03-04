package com.github.ruediste.salta.core;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.matchers.Matcher;

/**
 * Implementation of {@link CreationRule} delegating to a
 * {@link Function} and a {@link Matcher}
 * 
 */
public class CreationRuleImpl implements CreationRule {

	private Function<CoreDependencyKey<?>, Supplier<Object>> supplierFactory;
	private Matcher<? super CoreDependencyKey<?>> matcher;

	public CreationRuleImpl(
			Matcher<? super CoreDependencyKey<?>> matcher,
			Function<CoreDependencyKey<?>, Supplier<Object>> supplierFactory) {
		this.matcher = matcher;
		this.supplierFactory = supplierFactory;
	}

	@Override
	public SupplierRecipe apply(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		if (matcher.matches(key))
			return new SupplierRecipeImpl(supplierFactory.apply(key));
		else
			return null;
	}

}
