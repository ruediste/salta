package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.github.ruediste.salta.core.CompiledFunction;
import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.SaltaException;

public class RecipeCompiler {
    private static final AtomicInteger instanceCounter = new AtomicInteger();

    private static class CompilerClassLoader extends ClassLoader {
        public CompilerClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bb) {
            return defineClass(name, bb, 0, bb.length);
        }
    }

    private final int instanceNr;

    public RecipeCompiler() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public RecipeCompiler(ClassLoader generatedCodeParentClassLoader) {
        instanceNr = instanceCounter.incrementAndGet();
        loader = new CompilerClassLoader(generatedCodeParentClassLoader);
    }

    private final CompilerClassLoader loader;
    private AtomicInteger classNumber = new AtomicInteger();

    /**
     * Compile a recipe. May be called from multiple threads. Calling thread
     * must holde the {@link CoreInjector#recipeLock}
     */
    public CompiledSupplier compileSupplier(SupplierRecipe recipe) {
        ClassCompilationContext ccc = createClass(CompiledSupplier.class);
        ccc.addMethod(ACC_PUBLIC, "get", "()Ljava/lang/Object;", null, new MethodRecipe() {

            @Override
            protected void compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {

                Class<?> producedType = recipe.compile(ctx);

                if (producedType.isPrimitive())
                    mv.box(Type.getType(producedType));

                mv.visitInsn(ARETURN);
            }
        });

        Class<?> cls = loadClass(ccc);

        return (CompiledSupplier) instantiate(cls);
    }

    /**
     * Compile a recipe which takes a parameter. Calling thread must holde the
     * {@link CoreInjector#recipeLock}
     */
    public CompiledFunction compileFunction(FunctionRecipe recipe) {
        ClassCompilationContext ccc = createClass(CompiledFunction.class);
        ccc.addMethod(ACC_PUBLIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new MethodRecipe() {

            @Override
            protected void compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {

                mv.loadArg(0);

                Class<?> producedType = recipe.compile(Object.class, ctx);

                if (producedType.isPrimitive())
                    mv.box(Type.getType(producedType));

                mv.visitInsn(ARETURN);
            }
        });
        Class<?> cls = loadClass(ccc);
        return (CompiledFunction) instantiate(cls);
    }

    public Class<?> loadClass(ClassCompilationContext ctx) {
        // generate bytecode
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ctx.getClazz().accept(cw);

        // load class
        Class<?> cls;
        byte[] bb = cw.toByteArray();
        String className = Type.getObjectType(ctx.getClazz().name).getClassName();

        // try {
        // Files.write(bb, new File("target/compiledRecipes/" + ctx.clazz.name
        // + ".class"));
        // } catch (IOException e2) {
        // throw new SaltaException("Error while writing generated class", e2);
        // }

        try {
            cls = getLoader().defineClass(className, bb);
            ctx.initFields(cls);
        } catch (Throwable e) {
            System.out.println("Error while loading compiled recipe class");
            ClassReader cr = new ClassReader(bb);
            cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), 0);
            CheckClassAdapter.verify(cr, false, new PrintWriter(System.err));

            throw new SaltaException("Error while loading compiled recipe", e);
        }

        // return result
        return cls;
    }

    /**
     * Instantiate the given class by callint its no args constructor.
     */
    public Object instantiate(Class<?> cls) {

        Object instance;
        try {
            Constructor<?> constructor = cls.getConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (Throwable e) {
            throw new SaltaException("Error while instantiating compiled recipe", e);
        }

        return instance;
    }

    public ClassCompilationContext createClass(Class<?> implementedInterface) {
        // setup clazz
        ClassNode clazz = new ClassNode();
        clazz.name = "salta/CompiledCreationRecipe" + instanceNr + "_" + classNumber.incrementAndGet();
        clazz.access = ACC_FINAL & ACC_PUBLIC & ACC_SYNTHETIC;
        clazz.version = V1_7;
        clazz.superName = Type.getInternalName(Object.class);
        if (implementedInterface != null)
            clazz.interfaces.add(Type.getInternalName(implementedInterface));

        // generate constructor
        generateConstructor(clazz);

        return new ClassCompilationContext(clazz, false, this);
    }

    private void generateConstructor(ClassVisitor cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(5, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public CompilerClassLoader getLoader() {
        return loader;
    }

}
