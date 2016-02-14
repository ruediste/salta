package com.github.ruediste.salta.core.compile;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Implementation of {@link SupplierRecipe} returning a fixed result
 */
public class ConstantSupplierRecipe<T> extends SupplierRecipe {

    private Class<? super T> valueClass;
    private T value;

    public ConstantSupplierRecipe(Class<? super T> valueClass, T value) {
        this.valueClass = valueClass;
        this.value = value;

    }

    @Override
    protected Class<?> compileImpl(GeneratorAdapter mv,
            MethodCompilationContext ctx) {
        ctx.addFieldAndLoad(valueClass, value);
        return valueClass;
    }

}
