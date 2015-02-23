package com.github.ruediste.salta.standard.binder;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public class BindingBuilderData<T> {
	public DefaultCreationRecipeBuilder recipeBuilder;
	public StandardStaticBinding binding;
	public StandardInjectorConfiguration config;

	private Matcher<CoreDependencyKey<?>> typeMatcher = key -> true;
	private Matcher<CoreDependencyKey<?>> annotationMatcher = key -> true;
	/**
	 * Dependency to be used to trigger an eager instantiation
	 */
	public DependencyKey<T> eagerInstantiationDependency;
	public Injector injector;
	public TypeToken<T> boundType;

	public BindingBuilderData() {
	}

	public void updateDepenencyMatcher() {
		binding.dependencyMatcher = getTypeMatcher()
				.and(getAnnotationMatcher());
	}

	public Matcher<CoreDependencyKey<?>> getTypeMatcher() {
		return typeMatcher;
	}

	public void setTypeMatcher(Matcher<CoreDependencyKey<?>> typeMatcher) {
		this.typeMatcher = typeMatcher;
		updateDepenencyMatcher();
	}

	public Matcher<CoreDependencyKey<?>> getAnnotationMatcher() {
		return annotationMatcher;
	}

	public void setAnnotationMatcher(
			Matcher<CoreDependencyKey<?>> annotationMatcher) {
		this.annotationMatcher = annotationMatcher;
		updateDepenencyMatcher();
	}
}