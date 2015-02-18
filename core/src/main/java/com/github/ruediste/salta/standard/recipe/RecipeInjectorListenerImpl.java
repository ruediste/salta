package com.github.ruediste.salta.standard.recipe;

import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;

/**
 * {@link RecipeInjectorListener} implementation accepting various strategy
 * classes. Allows clients to avoid implementing bytecode generation.
 */
public class RecipeInjectorListenerImpl implements RecipeInjectionListener {

	private Function<Object, Object> listener;
	private AroundListener aroundListener;
	private Function<Supplier<Object>, Object> wrapperListener;

	private RecipeInjectorListenerImpl(Function<Object, Object> listener,
			AroundListener aroundListener,
			Function<Supplier<Object>, Object> wrapperListener) {
		super();
		this.listener = listener;
		this.aroundListener = aroundListener;
		this.wrapperListener = wrapperListener;
	}

	public static RecipeInjectionListener of(Function<Object, Object> listener) {
		return new RecipeInjectorListenerImpl(listener, null, null);
	}

	public static RecipeInjectionListener of(AroundListener listener) {
		return new RecipeInjectorListenerImpl(null, listener, null);
	}

	public static RecipeInjectionListener ofWrapper(
			Function<Supplier<Object>, Object> listener) {
		return new RecipeInjectorListenerImpl(null, null, listener);
	}

	public interface AroundListener {
		void before();

		Object after(Object instance);
	}

	@Override
	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext,
			CreationRecipe innerRecipe) {
		if (listener != null) {
			compilationContext.addFieldAndLoad(
					Type.getDescriptor(Function.class), listener);
			innerRecipe.compile(mv, compilationContext);
			mv.invokeInterface(Type.getType(Function.class),
					Method.getMethod("Object apply(Object)"));
		} else if (aroundListener != null) {
			compilationContext.addFieldAndLoad(
					Type.getDescriptor(AroundListener.class), aroundListener);
			mv.dup();
			mv.invokeInterface(Type.getType(AroundListener.class),
					Method.getMethod("void before()"));
			innerRecipe.compile(mv, compilationContext);
			mv.invokeInterface(Type.getType(Function.class),
					Method.getMethod("Object apply(Object)"));
		} else if (wrapperListener != null) {
			compilationContext.addFieldAndLoad(
					Type.getDescriptor(Function.class), wrapperListener);
			compilationContext.compileToSupplier(innerRecipe);
			mv.invokeInterface(Type.getType(Function.class),
					Method.getMethod("Object apply(Object)"));
		}
	}

}
