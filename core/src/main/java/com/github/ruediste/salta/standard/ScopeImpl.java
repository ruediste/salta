package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * {@link Scope} implementation delegating to a {@link ScopeHandler}.
 */
public class ScopeImpl implements Scope {

    private ScopeHandler handler;

    public interface ScopeHandler {
        /**
         * Scope the given binding
         * 
         * @param supplier
         *            supplier of the unscoped instance
         * @param binding
         *            Binding beeing scoped. Use this as key to identify
         *            instances.
         * @param requestedKey
         *            Key beeing requested. Be aware that the same binding can
         *            be requested using different keys.
         */
        Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
                CoreDependencyKey<?> requestedKey);
    }

    public ScopeImpl(ScopeHandler handler) {
        this.handler = handler;
    }

    @Override
    public SupplierRecipe createRecipe(RecipeCreationContext ctx,
            Binding binding, CoreDependencyKey<?> requestedKey) {
        CompiledSupplier compilerInnerRecipe = ctx.getCompiler()
                .compileSupplier(binding.getOrCreateRecipe(ctx));

        Supplier<Object> scoped = handler.scope(compilerInnerRecipe::getNoThrow,
                binding, requestedKey);
        return new SupplierRecipe() {

            @Override
            protected Class<?> compileImpl(GeneratorAdapter mv,
                    MethodCompilationContext ctx) {
                ctx.addFieldAndLoad(Supplier.class, scoped);
                mv.invokeInterface(Type.getType(Supplier.class),
                        Method.getMethod("Object get()"));
                return Object.class;
            }
        };
    }
}
