package com.github.ruediste.salta.core;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * Contains the whole configuration of an injector.
 */
public class CoreInjectorConfiguration extends AttachedPropertyBearerBase {

	/**
	 * List of enhancer factories. The enhancers of all factories are combined.
	 */
	public final List<EnhancerFactory> enhancerFactories = new ArrayList<>();

	public List<RecipeEnhancer> createEnhancers(RecipeCreationContext ctx,
			CoreDependencyKey<?> requestedKey) {
		return enhancerFactories.stream()
				.map(r -> r.getEnhancer(ctx, requestedKey))
				.filter(x -> x != null).collect(toList());
	}

	public SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			RecipeCreationContext ctx, CoreDependencyKey<?> requestedKey) {
		return applyEnhancers(seedRecipe, createEnhancers(ctx, requestedKey));
	}

	public SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			List<RecipeEnhancer> enhancers) {
		SupplierRecipe result = seedRecipe;
		for (RecipeEnhancer enhancer : enhancers) {
			SupplierRecipe innerRecipe = result;
			result = new SupplierRecipe() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext ctx) {
					return enhancer.compile(ctx, innerRecipe);
				}

			};
		}
		return result;
	}
}
