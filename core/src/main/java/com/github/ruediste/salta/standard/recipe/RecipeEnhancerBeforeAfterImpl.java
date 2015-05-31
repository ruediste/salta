package com.github.ruediste.salta.standard.recipe;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.RecipeEnhancer;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * {@link RecipeEnhancer} implementation delegating to a strategy object. Allows
 * clients to implement bytecode generation.
 */
public class RecipeEnhancerBeforeAfterImpl implements RecipeEnhancer {

    private BeforeAfterEnhancer beforeAfterEnhancer;

    public RecipeEnhancerBeforeAfterImpl(BeforeAfterEnhancer beforeAfterEnhancer) {
        this.beforeAfterEnhancer = beforeAfterEnhancer;
    }

    public interface BeforeAfterEnhancer {
        void before();

        Object after(Object instance);
    }

    @Override
    public Class<?> compile(MethodCompilationContext compilationContext,
            SupplierRecipe innerRecipe) {
        GeneratorAdapter mv = compilationContext.getMv();

        compilationContext.addFieldAndLoad(BeforeAfterEnhancer.class,
                beforeAfterEnhancer);
        mv.dup();
        mv.invokeInterface(Type.getType(BeforeAfterEnhancer.class),
                Method.getMethod("void before()"));
        Class<?> t = innerRecipe.compile(compilationContext);
        if (t.isPrimitive())
            mv.box(Type.getType(t));
        mv.invokeInterface(Type.getType(BeforeAfterEnhancer.class),
                Method.getMethod("Object after(Object)"));
        return Object.class;
    }

}
