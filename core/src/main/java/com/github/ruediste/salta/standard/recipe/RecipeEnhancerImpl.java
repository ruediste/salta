package com.github.ruediste.salta.standard.recipe;

import java.util.function.Function;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.RecipeEnhancer;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * {@link RecipeEnhancer} implementation delegating to a {@link Function}
 */
public class RecipeEnhancerImpl implements RecipeEnhancer {

	private Function<Object, Object> function;

	public RecipeEnhancerImpl(Function<Object, Object> function) {
		this.function = function;
	}

	@Override
	public Class<?> compile(MethodCompilationContext ctx,
			SupplierRecipe innerRecipe) {
		ctx.addFieldAndLoad(Function.class, function);
		Class<?> tos = innerRecipe.compile(ctx);
		if (tos.isPrimitive())
			ctx.getMv().box(Type.getType(tos));
		ctx.getMv().invokeInterface(Type.getType(Function.class),
				Method.getMethod("Object apply(Object)"));
		return Object.class;
	}
}
