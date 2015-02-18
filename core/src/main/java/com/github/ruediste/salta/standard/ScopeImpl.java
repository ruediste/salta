package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.google.common.reflect.TypeToken;

public class ScopeImpl implements Scope {

	private ScopeHanlder handler;

	public interface ScopeHanlder {
		Object scope(Supplier<Object> supplier, Binding binding,
				TypeToken<?> type);
	}

	public ScopeImpl(ScopeHanlder handler) {
		this.handler = handler;
	}

	@Override
	public CreationRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> type, CreationRecipe innerRecipe) {
		return new CreationRecipe() {

			@Override
			public void compile(GeneratorAdapter mv,
					RecipeCompilationContext compilationContext) {
				compilationContext.addFieldAndLoad(ScopeHanlder.class, handler);
				compilationContext.compileToSupplier(innerRecipe);
				compilationContext.addFieldAndLoad(Binding.class, binding);
				compilationContext.addFieldAndLoad(TypeToken.class, type);
				mv.invokeInterface(
						Type.getType(ScopeHanlder.class),
						new Method("scope", Type.getType(Object.class),
								new Type[] { Type.getType(Supplier.class),
										Type.getType(Binding.class),
										Type.getType(TypeToken.class) }));
			}
		};
	}
}
