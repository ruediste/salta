package com.github.ruediste.salta.standard.recipe;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;

/**
 * Implementation of {@link RecipeInstantiator} using a {@link Supplier} to
 * generate the result
 */
public class RecipeInstantiatorImpl extends RecipeInstantiator {

    private Supplier<Object> supplier;

    public RecipeInstantiatorImpl(Supplier<Object> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected Class<?> compileImpl(GeneratorAdapter mv,
            MethodCompilationContext ctx) {
        ctx.addFieldAndLoad(Supplier.class, supplier);
        mv.invokeInterface(Type.getType(Supplier.class),
                Method.getMethod("Object get()"));

        return Object.class;
    }

}
