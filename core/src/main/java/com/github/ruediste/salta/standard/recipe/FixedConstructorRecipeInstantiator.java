package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;

/**
 * Instantiate a fixed class using a fixed constructor. Use a subclass of
 * {@link ConstructorInstantiatorRuleBase} to create an instance
 */
public class FixedConstructorRecipeInstantiator extends RecipeInstantiator {

    Constructor<?> constructor;
    List<SupplierRecipe> argumentDependencies;

    public FixedConstructorRecipeInstantiator(Constructor<?> constructor, List<SupplierRecipe> argumentDependencies) {
        constructor.setAccessible(true);
        this.constructor = constructor;
        this.argumentDependencies = new ArrayList<>(argumentDependencies);
    }

    @Override
    public Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
        if (Accessibility.isConstructorAccessible(constructor, ctx.getCompiledCodeClassLoader()))
            return compileDirect(mv, ctx);
        else
            return compileDynamic(mv, ctx);
    }

    private Class<?> compileDirect(GeneratorAdapter mv, MethodCompilationContext compilationContext) {

        mv.newInstance(Type.getType(constructor.getDeclaringClass()));
        mv.dup();

        // push dependencies

        for (int i = 0; i < argumentDependencies.size(); i++) {
            SupplierRecipe dependency = argumentDependencies.get(i);
            Class<?> tosType = dependency.compile(compilationContext);
            Class<?> argType = constructor.getParameterTypes()[i];
            if (!argType.isAssignableFrom(tosType)) {
                if (argType.isPrimitive())
                    mv.unbox(Type.getType(argType));
                else
                    mv.checkCast(Type.getType(argType));
            }
        }

        // call constructor
        mv.invokeConstructor(Type.getType(constructor.getDeclaringClass()), Method.getMethod(constructor));

        return constructor.getDeclaringClass();
    }

    private Class<?> compileDynamic(GeneratorAdapter mv, MethodCompilationContext ctx) {

        Class<?> resultType = constructor.getDeclaringClass();
        if (!Accessibility.isClassAccessible(resultType, ctx.getCompiledCodeClassLoader()))
            resultType = Object.class;

        // push arguments
        Type[] argTypes = new Type[argumentDependencies.size()];
        for (int i = 0; i < argumentDependencies.size(); i++) {
            Class<?> t = argumentDependencies.get(i).compile(ctx);
            argTypes[i] = Type.getType(ctx.castToPublic(t, constructor.getParameterTypes()[i]));
        }

        Type[] origArgTypes = new Type[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            origArgTypes[i] = Type.getType(constructor.getParameterTypes()[i]);
        }

        String bootstrapDesc = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;";

        // call
        Handle bsm = new Handle(H_INVOKESTATIC, Type.getInternalName(FixedConstructorRecipeInstantiator.class),
                "bootstrap", bootstrapDesc);
        mv.invokeDynamic("init", Type.getMethodDescriptor(Type.getType(resultType), argTypes), bsm,
                ctx.addField(Constructor.class, constructor).getName());

        return resultType;
    }

    public static CallSite bootstrap(Lookup lookup, String methodName, MethodType type, String constructorFieldName)
            throws Exception {
        Field field = lookup.lookupClass().getField(constructorFieldName);
        field.setAccessible(true);
        Constructor<?> constructor = (Constructor<?>) field.get(null);
        MethodHandle handle = lookup.unreflectConstructor(constructor);
        return new ConstantCallSite(handle.asType(type));
    }
}
