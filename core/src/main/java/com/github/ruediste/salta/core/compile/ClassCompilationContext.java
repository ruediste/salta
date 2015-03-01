package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import java.util.ArrayList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Context for the compilation of a recipe class. Manages unique names of fields
 * and methods
 */
public class ClassCompilationContext {

	private final ClassNode clazz;

	public static class FieldEntry {
		String name;
		Object value;
	}

	ArrayList<FieldEntry> fields = new ArrayList<>();
	ArrayList<Runnable> queuedActions = new ArrayList<>();

	private int methodNr;

	private final RecipeCompiler compiler;

	public ClassCompilationContext(ClassNode clazz, RecipeCompiler compiler) {
		this.clazz = clazz;
		this.compiler = compiler;
	}

	public <T> FieldHandle addField(Class<T> fieldType, T value) {
		FieldEntry entry = new FieldEntry();
		entry.name = "field" + fields.size();
		entry.value = value;
		fields.add(entry);

		getClazz().visitField(ACC_PUBLIC + ACC_STATIC, entry.name,
				Type.getDescriptor(fieldType), null, null);

		return new FieldHandle(fieldType, entry.name);
	}

	public void queueAction(Runnable runnable) {
		queuedActions.add(runnable);
	}

	public RecipeCompiler getCompiler() {
		return compiler;
	}

	/**
	 * Constructs a new {@link MethodNode}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the
	 * {@link #MethodNode(int, int, String, String, String, String[])} version.
	 * 
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param name
	 *            name of the method
	 * @param desc
	 *            the method's descriptor (see {@link Type}).
	 * @param exceptions
	 *            the internal names of the method's exception classes (see
	 *            {@link Type#getInternalName() getInternalName}). May be
	 *            <tt>null</tt>.
	 * @throws IllegalStateException
	 *             If a subclass calls this constructor.
	 * @return name of the generated method
	 */
	public String addMethod(final int access, final String desc,
			final String[] exceptions, MethodRecipe recipe) {
		return addMethod(access, "method" + methodNr++, desc, exceptions,
				recipe);

	}

	/**
	 * Constructs a new {@link MethodNode}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the
	 * {@link #MethodNode(int, int, String, String, String, String[])} version.
	 * 
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param desc
	 *            the method's descriptor (see {@link Type}).
	 * @param exceptions
	 *            the internal names of the method's exception classes (see
	 *            {@link Type#getInternalName() getInternalName}). May be
	 *            <tt>null</tt>.
	 * @throws IllegalStateException
	 *             If a subclass calls this constructor.
	 * @return name of the generated method
	 */
	public String addMethod(final int access, String name, final String desc,
			final String[] exceptions, MethodRecipe recipe) {
		MethodNode m = new MethodNode(access, name, desc, null, exceptions);
		getClazz().methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		mv.visitCode();
		recipe.compile(new MethodCompilationContext(this, mv));
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return m.name;
	}

	public String getInternalClassName() {
		return getClazz().name;
	}

	public ClassNode getClazz() {
		return clazz;
	}
}
