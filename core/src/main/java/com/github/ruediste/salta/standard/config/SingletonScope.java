package com.github.ruediste.salta.standard.config;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.attachedProperties.AttachedProperty;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;

public class SingletonScope implements Scope {

	private static AttachedProperty<Binding, Object> instance = new AttachedProperty<>("singleton instance");

	@Override
	public String toString() {
		return "Singleton";
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx, Binding binding, CoreDependencyKey<?> requestedKey) {
		// make sure to create the instance when first creating the recipe
		instantiate(ctx, binding);

		return new SupplierRecipe() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {

				Class<?> fieldType = requestedKey.getRawType();
				if (!Accessibility.isClassAccessible(fieldType, ctx.getCompiledCodeClassLoader())) {
					fieldType = Object.class;
				}
				ctx.addFieldAndLoad((Class) fieldType, instance.get(binding));
				return fieldType;
			}
		};
	}

	@Override
	public void performEagerInstantiation(RecipeCreationContext ctx, Binding binding) {
		instantiate(ctx, binding);
	}

	/**
	 * Instantiate the instance
	 */
	public void instantiate(RecipeCreationContext ctx, Binding binding) {
		if (!instance.isSet(binding)) {
			SupplierRecipe innerRecipe = binding.getOrCreateRecipe(ctx);
			try {
				instance.set(binding, ctx.getCompiler().compileSupplier(innerRecipe).get());
			} catch (Throwable t) {
				throw new SaltaException("Error while instantiating instance for " + binding, t);
			}
		}
	}
}