package com.github.ruediste.salta.standard.recipe;

import java.util.function.Function;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.RecipeCompilationContext;

public class RecipeInjectorListenerImpl implements RecipeInjectionListener {

	private Function<Object, Object> listener;

	public RecipeInjectorListenerImpl(Function<Object, Object> listener) {
		this.listener = listener;
	}

	@Override
	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		compilationContext.addAndLoad(Type.getDescriptor(Function.class),
				listener);
		mv.swap();
		mv.invokeInterface(Type.getType(Function.class),
				Method.getMethod("Object apply(Object)"));
	}

}
