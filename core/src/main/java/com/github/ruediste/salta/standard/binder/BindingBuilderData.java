package com.github.ruediste.salta.standard.binder;

import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

public class BindingBuilderData<T> {
	public DefaultCreationRecipeBuilder recipeBuilder;
	public StandardStaticBinding binding;
	public StandardInjectorConfiguration config;
	/**
	 * Dependency to be used to trigger an eager instantiation
	 */
	public DependencyKey<T> eagerInstantiationDependency;
	public Injector injector;

	public BindingBuilderData() {
	}
}