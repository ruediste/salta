package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

public class ScopeImpl implements Scope {

	private ScopeHandler handler;

	public interface ScopeHandler {
		/**
		 * Scope the given binding
		 * 
		 * @param supplier
		 *            supplier of the unscoped instance
		 * @param binding
		 *            binding beeing scoped
		 * @param type
		 *            type the binding was created for
		 * @return
		 */
		Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
				TypeToken<?> type);
	}

	public ScopeImpl(ScopeHandler handler) {
		this.handler = handler;
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> type, SupplierRecipe innerRecipe) {
		CompiledSupplier compilerInnerRecipe = ctx.getCompiler()
				.compileSupplier(innerRecipe);

		Supplier<Object> scoped = handler.scope(
				compilerInnerRecipe::getNoThrow, binding, type);
		return new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {
				ctx.addFieldAndLoad(Supplier.class, scoped);
				mv.invokeInterface(Type.getType(Supplier.class),
						Method.getMethod("Object get("));
				return Object.class;
			}
		};
	}
}
