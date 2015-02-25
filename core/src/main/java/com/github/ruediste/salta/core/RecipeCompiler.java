package com.github.ruediste.salta.core;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.github.ruediste.salta.core.RecipeCompilationContextBase.FieldEntry;
import com.google.common.io.Files;

public class RecipeCompiler {
	private static final AtomicInteger instanceCounter = new AtomicInteger();

	private static class CompilerClassLoader extends ClassLoader {
		public Class<?> defineClass(String name, byte[] bb) {
			return defineClass(name, bb, 0, bb.length);
		}
	}

	private final int instanceNr;

	public RecipeCompiler() {
		instanceNr = instanceCounter.incrementAndGet();
	}

	private CompilerClassLoader loader = new CompilerClassLoader();
	private AtomicInteger classNumber = new AtomicInteger();

	/**
	 * Compile a recipe. May be called from multiple threads. May not acquire
	 * {@link CoreInjector#recipeLock} or {@link CoreInjector#instantiationLock}
	 */
	public CompiledSupplier compileSupplier(SupplierRecipe recipe) {
		RecipeCompilationContextImpl ctx = new RecipeCompilationContextImpl(
				this);

		setupClazz(ctx, CompiledSupplier.class);

		// compile method
		MethodNode m = new MethodNode(ACC_PUBLIC, "get",
				"()Ljava/lang/Object;", null, null);
		ctx.clazz.methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		ctx.mv = mv;
		mv.visitCode();

		Class<?> producedType = recipe.compile(ctx);

		if (producedType.isPrimitive())
			mv.box(Type.getType(producedType));

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		return (CompiledSupplier) loadAndInstantiate(ctx);
	}

	/**
	 * Compile a recipe which takes a parameter. May be called from multiple
	 * threads. May not acquire {@link CoreInjector#recipeLock} or
	 * {@link CoreInjector#instantiationLock}
	 */
	public CompiledFunction compileFunction(FunctionRecipe recipe) {
		RecipeCompilationContextImpl ctx = new RecipeCompilationContextImpl(
				this);

		setupClazz(ctx, CompiledFunction.class);

		// compile method
		MethodNode m = new MethodNode(ACC_PUBLIC, "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		ctx.clazz.methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		ctx.mv = mv;
		mv.visitCode();
		mv.loadArg(0);

		Class<?> producedType = recipe.compile(Object.class, ctx);

		if (producedType.isPrimitive())
			mv.box(Type.getType(producedType));

		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		return (CompiledFunction) loadAndInstantiate(ctx);
	}

	private Object loadAndInstantiate(RecipeCompilationContextImpl ctx) {
		// generate bytecode
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ctx.clazz.accept(cw);

		// load and instantiate
		Object instance;
		Class<?> cls;
		byte[] bb = cw.toByteArray();
		String className = Type.getObjectType(ctx.clazz.name).getClassName();

		try {
			Files.write(bb, new File("target/compiledRecipes/" + ctx.clazz.name
					+ ".class"));
		} catch (IOException e2) {
			throw new SaltaException("Error while writing generated class", e2);
		}
		try {
			cls = loader.defineClass(className, bb);
			Constructor<?> constructor = cls.getConstructor();
			constructor.setAccessible(true);
			instance = constructor.newInstance();
		} catch (Throwable e) {
			System.out.println("Error while loading compiled recipe");
			ClassReader cr = new ClassReader(bb);
			cr.accept(new TraceClassVisitor(null, new ASMifier(),
					new PrintWriter(System.out)), 0);
			CheckClassAdapter.verify(cr, false, new PrintWriter(System.err));

			throw new SaltaException("Error while loading compiled recipe", e);
		}

		Field modifiersField;
		try {
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e1) {
			throw new SaltaException(e1);
		}
		// init fields
		for (FieldEntry entry : ctx.fields) {
			try {
				Field field = cls.getField(entry.name);
				field.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers()
						& ~Modifier.FINAL);
				field.set(null, entry.value);
			} catch (Exception e) {
				throw new SaltaException("Error while setting parameter "
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
		clazz.name = "salta/CompiledCreationRecipe" + instanceNr + "_"
				+ classNumber.incrementAndGet();
		clazz.access = ACC_FINAL & ACC_PUBLIC & ACC_SYNTHETIC;
		clazz.version = V1_7;
		clazz.superName = Type.getInternalName(Object.class);
		clazz.interfaces.add(Type.getInternalName(implementedInterface));

		// generate constructor
		generateConstructor(clazz);
	}

	private void generateConstructor(ClassVisitor cw) {
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

	/**
	 * see {@link RecipeCompilationContext#cast(Class, Class)}
	 */
	public void cast(GeneratorAdapter mv, Class<?> from, Class<?> to) {
		if (from.equals(to))
			return;

		if (from.isPrimitive() && to.isPrimitive()) {
			// two primitives
			// fall throught to throw
		} else if (from.isPrimitive()) {
			if (!to.isArray() && !to.isPrimitive()) {
				// primitive to object
				mv.box(Type.getType(from));
				return;
			}
		} else if (to.isPrimitive()) {
			if (!from.isArray()) {
				// any to primitive
				mv.unbox(Type.getType(to));
				return;
			}
		} else {
			if (!to.isAssignableFrom(from))
				// downcast
				mv.checkCast(Type.getType(to));
			return;
		}
		throw new SaltaException("Cannot cast from " + from + " to " + to);
	}
}
