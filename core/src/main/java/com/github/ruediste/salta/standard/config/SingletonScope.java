package com.github.ruediste.salta.standard.config;

import java.util.function.Function;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;
import com.google.common.reflect.TypeToken;

public class SingletonScope implements Scope {

	private static AttachedProperty<Binding, Object> instance = new AttachedProperty<>(
			"singleton instance");

	@Override
	public String toString() {
		return "Singleton";
	}

	@Override
	public Function<RecipeCreationContext, SupplierRecipe> createRecipe(
			Binding binding, TypeToken<?> requestedType) {
		return ctx -> {
			// make sure to create the instance when first creating the recipe
			instantiate(ctx, binding);

			return new SupplierRecipe() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext ctx) {

					Class<?> fieldType = requestedType.getRawType();
					if (!Accessibility.isClassPublic(fieldType)) {
						fieldType = Object.class;
					}
					ctx.addFieldAndLoad((Class) fieldType,
							instance.get(binding));
					return fieldType;
				}
			};
		};
	}

	@Override
	public void performEagerInstantiation(RecipeCreationContext ctx,
			Binding binding) {
		instantiate(ctx, binding);
	}

	/**
	 * Instantiate the instance
	 */
	public void instantiate(RecipeCreationContext ctx, Binding binding) {
		if (!instance.isSet(binding)) {
			SupplierRecipe innerRecipe = binding.getOrCreateRecipe().apply(ctx);
			instance.set(binding, ctx.getCompiler()
					.compileSupplier(innerRecipe).getNoThrow());
		}
	}
}