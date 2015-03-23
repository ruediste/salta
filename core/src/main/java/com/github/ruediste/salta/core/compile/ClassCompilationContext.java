package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.github.ruediste.salta.core.SaltaException;

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

	private int methodNr;
	private final boolean codeSizeEvaluation;

	private final RecipeCompiler compiler;

	public ClassCompilationContext(ClassNode clazz, boolean codeSizeEvaluation,
			RecipeCompiler compiler) {
		this.clazz = clazz;
		this.codeSizeEvaluation = codeSizeEvaluation;
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

	/**
	 * Add a new Method to this class. The name will be generated by this
	 * context and is guaranteed not to conflict with any existing method.
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
	 * @param recipe
	 *            recipe to be used to compile the method
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
	 * Add a new Method to this class.
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
	 * @param recipe
	 *            recipe to be used to compile the method
	 * @throws IllegalStateException
	 *             If a subclass calls this constructor.
	 * @return name of the generated method
	 */
	public String addMethod(final int access, String name, final String desc,
			final String[] exceptions, MethodRecipe recipe) {
		if (codeSizeEvaluation)
			return name;
		MethodNode m = new MethodNode(access, name, desc, null, exceptions);
		getClazz().methods.add(m);
		GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(m.name,
				m.desc), m);
		mv.visitCode();
		recipe.compile(new MethodCompilationContext(this, mv, access, desc));
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

	public boolean isCodeSizeEvaluation() {
		return codeSizeEvaluation;
	}

	public void initFields(Class<?> recipeClass) {
		Field modifiersField;
		try {
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e1) {
			throw new SaltaException(e1);
		}
		// init fields
		for (FieldEntry entry : fields) {
			try {
				Field field = recipeClass.getField(entry.name);
				field.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers()
						& ~Modifier.FINAL);
				field.set(null, entry.value);
			} catch (Exception e) {
				throw new SaltaException("Error while setting parameter "
						+ entry.name, e);
			}
		}
	}

	public RecipeCompiler getCompiler() {
		return compiler;
	}

}
