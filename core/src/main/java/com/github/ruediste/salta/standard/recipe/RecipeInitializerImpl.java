package com.github.ruediste.salta.standard.recipe;

import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;

/**
 * Implementation of {@link RecipeInitializer} delegating to a {@link Consumer},
 * allowing clients to avoid implementing byte code generation.
 */
public class RecipeInitializerImpl implements RecipeInitializer {

	private Consumer<Object> consumer;

	public RecipeInitializerImpl(Consumer<Object> consumer) {
		this.consumer = consumer;
	}

	@Override
	public Class<?> compileImpl(Class<?> argumentType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {
		mv.dup();
		ctx.addFieldAndLoad(Consumer.class, consumer);
		mv.swap();
		if (argumentType.isPrimitive())
			mv.box(Type.getType(argumentType));
		mv.invokeInterface(Type.getType(Consumer.class),
				Method.getMethod("void accept(Object)"));
		return argumentType;
	}

}
