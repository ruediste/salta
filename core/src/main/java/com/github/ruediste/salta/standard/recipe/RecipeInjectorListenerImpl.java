package com.github.ruediste.salta.standard.recipe;

import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

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
	public Class<?> compile(MethodCompilationContext compilationContext,
			SupplierRecipe innerRecipe) {
		GeneratorAdapter mv = compilationContext.getMv();
		if (listener != null) {
			compilationContext.addFieldAndLoad(Function.class, listener);
			Class<?> t = innerRecipe.compile(compilationContext);
			if (t.isPrimitive())
				mv.box(Type.getType(t));
			mv.invokeInterface(Type.getType(Function.class),
					Method.getMethod("Object apply(Object)"));
			return Object.class;
		} else if (aroundListener != null) {
			compilationContext.addFieldAndLoad(AroundListener.class,
					aroundListener);
			mv.dup();
			mv.invokeInterface(Type.getType(AroundListener.class),
					Method.getMethod("void before()"));
			Class<?> t = innerRecipe.compile(compilationContext);
			if (t.isPrimitive())
				mv.box(Type.getType(t));
			mv.invokeInterface(Type.getType(Function.class),
					Method.getMethod("Object apply(Object)"));
			return Object.class;
		} else if (wrapperListener != null) {
			compilationContext.addFieldAndLoad(Function.class, wrapperListener);
			compilationContext.compileToSupplier(innerRecipe);
			mv.invokeInterface(Type.getType(Function.class), Method
					.getMethod("Object apply(java.util.function.Supplier)"));
			return Object.class;
		} else
			throw new SaltaException("should not happen");
	}

}
