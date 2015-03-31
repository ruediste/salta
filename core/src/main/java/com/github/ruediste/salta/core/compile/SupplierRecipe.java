package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.compile.MethodCompilationContext.CodeSizeHelper;

/**
 * Recipe for compiling a supplier of a value
 */
public abstract class SupplierRecipe {

	/**
	 * Default size threshold. If the compiled size of a recipe is bigger than
	 * this threshold, it will be compiled to a separate class. Values bigger
	 * 64k will result in too big methods (JVM refuses them), too small values
	 * result in a large number of classes.
	 */
	public static int DEFAULT_SIZE_THRESHOLD = 7000;// leave some extra space

	private final int sizeThreshold;
	private int codeSize = -1;
	private boolean separateSubRecipes;
	private Class<?> returnType;

	public SupplierRecipe() {
		this(DEFAULT_SIZE_THRESHOLD);
	}

	public SupplierRecipe(int sizeThreshold) {
		this.sizeThreshold = sizeThreshold;
	}

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack. Generally, there is an implicit expected type
	 * for the TOS value. However, the produced value does not need to have the
	 * expected type, since this is often not possible due to types which are
	 * not visible from the compiled recipe. The caller of compile has to allow
	 * for all typecasts which are performed by
	 * {@link MethodCompilationContext#castToPublic(Class, Class)}.
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	public final Class<?> compile(MethodCompilationContext ctx) {
		if (codeSize == -1) {
			// evaluate size first
			CodeSizeHelper helper = ctx.getCodeSizeHelper();

			returnType = compileImpl(helper.ctx.getMv(), helper.ctx);
			codeSize = helper.getSize();

			if (helper.getSize() > sizeThreshold) {
				// code is too big, we have to separate all sub recipes
				separateSubRecipes = true;
				CodeSizeHelper innerHelper = ctx.getCodeSizeHelper();
				returnType = innerHelper.ctx.withSeparateSubRecipes(true,
						mv -> compileImpl(mv, innerHelper.ctx));
				codeSize = innerHelper.getSize();

				// do a cast to public, such that we can generate a method
				// afterwards. This modifies the code size again, therefore the
				// size was saved before
				returnType = innerHelper.ctx.castToPublic(returnType,
						returnType);
			}

		}

		if (ctx.getClassCtx().isCodeSizeEvaluation()) {
			ctx.addCodeSizeOffset(codeSize);
			return returnType;
		}

		if (ctx.isSeparateSubRecipes()) {
			ClassCompilationContext ccc = ctx.getClassCtx().getCompiler()
					.createClass(null);

			String desc = Type.getMethodDescriptor(Type.getType(returnType));
			String methodName = ccc.addMethod(ACC_PUBLIC + ACC_STATIC, desc,
					null, new MethodRecipe() {

						@Override
						protected void compileImpl(GeneratorAdapter mv,
								MethodCompilationContext ctx) {
							SupplierRecipe.this.compileImpl(mv, ctx);
							mv.returnValue();
						}
					});
			ctx.getClassCtx().getCompiler().loadClass(ccc);

			ctx.getMv().visitMethodInsn(INVOKESTATIC,
					ccc.getInternalClassName(), methodName, desc, false);
			return returnType;
		} else
			return ctx.withSeparateSubRecipes(separateSubRecipes,
					mv -> compileImpl(mv, ctx));
	}

	/**
	 * Emit the code to produce the supplied instance, which needs to be placed
	 * on the top of the stack.
	 * 
	 * @return type of the supplied instance
	 * 
	 */
	protected abstract Class<?> compileImpl(GeneratorAdapter mv,
			MethodCompilationContext ctx);

}
