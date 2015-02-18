package com.github.ruediste.salta.core;

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
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContextBase.FieldEntry;

public class CreationRecipeCompiler {

	private static class CompilerClassLoader extends ClassLoader {
		public Class<?> defineClass(String name, byte[] bb) {
			return defineClass(name, bb, 0, bb.length);
		}
	}

	CompilerClassLoader loader = new CompilerClassLoader();
	AtomicInteger classNumber = new AtomicInteger();

	/**
	 * Compile a recipe. May be called from multiple threads. May not acquire
	 * {@link CoreInjector#recipeLock} or {@link CoreInjector#instantiationLock}
	 */
	public CompiledCreationRecipe compile(CreationRecipe recipe) {
		RecipeCompilationContextImpl ctx = new RecipeCompilationContextImpl(
				this);

		setupClazz(ctx, CompiledCreationRecipe.class);

		// compile method
		MethodNode m = new MethodNode(ACC_PUBLIC, "get",
				"()Ljava/lang/Object;", null, null);
		ctx.clazz.methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		ctx.mv = mv;
		mv.visitCode();

		recipe.compile(mv, ctx);

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		return (CompiledCreationRecipe) loadAndInstantiate(ctx);
	}

	/**
	 * Compile a recipe which takes a parameter. May be called from multiple
	 * threads. May not acquire {@link CoreInjector#recipeLock} or
	 * {@link CoreInjector#instantiationLock}
	 */
	public CompiledParameterizedCreationRecipe compileParameterizedRecipe(
			CreationRecipe recipe) {
		RecipeCompilationContextImpl ctx = new RecipeCompilationContextImpl(
				this);

		setupClazz(ctx, CompiledParameterizedCreationRecipe.class);

		// compile method
		MethodNode m = new MethodNode(ACC_PUBLIC, "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		ctx.clazz.methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		ctx.mv = mv;
		mv.visitCode();

		recipe.compile(mv, ctx);

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		return (CompiledParameterizedCreationRecipe) loadAndInstantiate(ctx);
	}

	private Object loadAndInstantiate(RecipeCompilationContextImpl ctx) {
		// generate bytecode
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ctx.clazz.accept(cw);
		if (true) {
			ClassReader cr = new ClassReader(cw.toByteArray());
			CheckClassAdapter.verify(cr, false, new PrintWriter(System.err));
			// cr.accept(new TraceClassVisitor(null, new ASMifier(),
			// new PrintWriter(System.out)), 0);
		}

		// load and instantiate
		Object instance;
		Class<?> cls;
		try {
			cls = loader.defineClass(ctx.clazz.name, cw.toByteArray());
			Constructor<?> constructor = cls.getConstructor();
			constructor.setAccessible(true);
			instance = constructor.newInstance();
		} catch (Exception e) {
			throw new ProvisionException("Error while loading compiled recipe",
					e);
		}

		// init fields
		for (FieldEntry entry : ctx.fields) {
			try {
				Field field = cls.getField(entry.name);
				field.setAccessible(true);
				field.set(instance, entry.value);
			} catch (Exception e) {
				throw new ProvisionException("Error while setting parameter "
						+ entry.name, e);
			}
		}

		// process queued actions
		ctx.queuedActions.forEach(Runnable::run);

		// return result
		return instance;
	}

	private void setupClazz(RecipeCompilationContextImpl ctx,
			Class<?> implementedInterface) {
		// setup clazz
		ClassNode clazz = new ClassNode();
		ctx.clazz = clazz;
		clazz.name = "SaltaCompiledCreationRecipe"
				+ classNumber.incrementAndGet();
		clazz.access = ACC_FINAL & ACC_PUBLIC & ACC_SYNTHETIC;
		clazz.version = V1_7;
		clazz.superName = Type.getInternalName(Object.class);
		clazz.interfaces.add(Type.getInternalName(implementedInterface));

		// generate constructor
		generateConstructor(clazz);
	}

	public void generateConstructor(ClassVisitor cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
				null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(5, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
				false);
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		// mv.visitLocalVariable("this", "Lcom/github/ruediste/salta/core/Foo;",
		// null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
}