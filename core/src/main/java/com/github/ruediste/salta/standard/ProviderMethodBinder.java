package com.github.ruediste.salta.standard;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ANEWARRAY;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Scans an instance for methods which are used to create instances and binds
 * them.
 */
public abstract class ProviderMethodBinder {
	private StandardInjectorConfiguration config;

	public ProviderMethodBinder(StandardInjectorConfiguration config) {
		this.config = config;
	}

	public void bindProviderMethodsOf(Object instance) {
		for (Method m : instance.getClass().getDeclaredMethods()) {
			Annotation availableQualifier = config
					.getAvailableQualifier(getClass());
			if (!isProviderMethod(m)) {
				continue;
			}

			Type boundType = m.getGenericReturnType();
			Matcher<CoreDependencyKey<?>> matcher = CoreDependencyKey
					.typeMatcher(TypeToken.of(boundType)).and(
							config.requredQualifierMatcher(config
									.getAvailableQualifier(m)));

			config.config.staticBindings.add(new StaticBinding() {

				@Override
				protected CreationRecipe createRecipe(RecipeCreationContext ctx) {
					CreationRecipe[] args = new CreationRecipe[m
							.getParameterCount()];
					Parameter[] parameters = m.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						Parameter p = parameters[i];
						args[i] = ctx.getRecipe(new InjectionPoint<>(TypeToken
								.of(p.getType()), m, p, i));
					}
					m.setAccessible(true);
					return new CreationRecipe() {

						@Override
						public void compile(GeneratorAdapter mv,
								RecipeCompilationContext compilationContext) {

							compilationContext.addFieldAndLoad(Method.class, m);
							// push module instance to the stack
							if (Modifier.isStatic(m.getModifiers())) {
								mv.visitInsn(ACONST_NULL);
							} else {
								compilationContext.addFieldAndLoad(
										Object.class, instance);
							}
							// push dependencies as an array
							mv.push(args.length);
							mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
							for (int i = 0; i < args.length; i++) {
								mv.dup();
								mv.push(i);
								args[i].compile(mv, compilationContext);
								mv.visitInsn(AASTORE);
							}

							mv.invokeVirtual(
									org.objectweb.asm.Type
											.getType(Method.class),
									new org.objectweb.asm.commons.Method(
											"invoke",
											"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));

						}
					};
				}

				@Override
				public boolean matches(CoreDependencyKey<?> dependency) {
					return matcher.matches(dependency);
				}

				@Override
				public String toString() {
					return "ProviderMethodBinding " + m;
				}
			});
		}
	}

	protected abstract boolean isProviderMethod(Method m);
}
