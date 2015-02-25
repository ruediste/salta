package com.github.ruediste.salta.core;

import java.util.function.Supplier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

public interface RecipeCompilationContext {

	/**
	 * Add a field to the generated method and return it's name. The field will
	 * be initialized to the given value
	 */
	String addField(String desc, Object value);

	<T> String addFieldAndLoad(Class<T> fieldType, T value);

	/**
	 * Add a field to the compiled recipe class and load it.
	 * 
	 * @param desc
	 *            descriptor of the field
	 * @param value
	 *            value the field is initialized to
	 * @return name of the added field
	 */
	String addFieldAndLoad(String desc, Object value);

	/**
	 * Queue the compilation of a recipe
	 */
	void queueAction(Runnable run);

	/**
	 * Compile the supplied recipe to a method and load it as {@link Supplier}.
	 * The resulting {@link Supplier} instance will be at the top of the stack.
	 */
	void compileToSupplier(SupplierRecipe recipe);

	/**
	 * Return the {@link MethodVisitor} to compile the code to
	 * 
	 * @return
	 */
	GeneratorAdapter getMv();

	RecipeCompiler getCompiler();

	/**
	 * Supports the following casts
	 * <ul>
	 * <li>from a primitive type to the boxed version</li>
	 * <li>from any type to a primitive</li>
	 * <li>downcast from any type to any other</li>
	 * <li>from object to array</li>
	 * <li>array to Object (trivial)</li>
	 * </ul>
	 * 
	 * This method will generate the necessary instructions such that the TOS
	 * value has the requested type.
	 */
	void cast(Class<?> from, Class<?> to);

}