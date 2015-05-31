package com.github.ruediste.salta.standard.recipe;

import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.RecipeEnhancer;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * {@link RecipeEnhancer} implementation delegating to a wrapper. Allows clients
 * to implement bytecode generation.
 */
public class RecipeEnhancerWrapperImpl implements RecipeEnhancer {

    private Function<Supplier<Object>, Object> wrapperEnhancer;

    public RecipeEnhancerWrapperImpl(
            Function<Supplier<Object>, Object> wrapperListener) {
        this.wrapperEnhancer = wrapperListener;
    }

    @Override
    public Class<?> compile(MethodCompilationContext ctx,
            SupplierRecipe innerRecipe) {
        GeneratorAdapter mv = ctx.getMv();
        ctx.addFieldAndLoad(Function.class, wrapperEnhancer);
        ctx.compileToSupplier(innerRecipe);
        mv.invokeInterface(Type.getType(Function.class),
                Method.getMethod("Object apply(Object)"));
        return Object.class;
    }

}
