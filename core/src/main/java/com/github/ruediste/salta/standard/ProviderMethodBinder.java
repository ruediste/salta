package com.github.ruediste.salta.standard;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedMethodInvocationFunctionRecipe;
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

			if (!isProviderMethod(m)) {
				continue;
			}
			m.setAccessible(true);

			Type boundType = m.getGenericReturnType();
			Matcher<CoreDependencyKey<?>> matcher = CoreDependencyKey
					.typeMatcher(TypeToken.of(boundType)).and(
							config.requredQualifierMatcher(config
									.getAvailableQualifier(m)));

			config.config.staticBindings.add(new StaticBinding() {

				@Override
				protected SupplierRecipe createRecipe(RecipeCreationContext ctx) {
					ArrayList<SupplierRecipe> args = new ArrayList<>();
					Parameter[] parameters = m.getParameters();
					for (int i = 0; i < parameters.length; i++) {
						Parameter p = parameters[i];
						args.add(ctx.getRecipe(new InjectionPoint<>(TypeToken
								.of(p.getType()), m, p, i)));
					}
					FixedMethodInvocationFunctionRecipe methodRecipe = new FixedMethodInvocationFunctionRecipe(
							m, args, config.config.injectionStrategy);
					return new SupplierRecipe() {

						@Override
						protected Class<?> compileImpl(GeneratorAdapter mv,
								MethodCompilationContext ctx) {
							ctx.addFieldAndLoad(Object.class, instance);
							return methodRecipe.compile(Object.class, ctx);
						}
					};
				}

				@Override
				public String toString() {
					return "ProviderMethodBinding " + m;
				}

				@Override
				public Matcher<CoreDependencyKey<?>> getMatcher() {
					return matcher;
				}

				@Override
				protected Scope getScopeImpl() {
					return config.getScope(m);
				}
			});
		}
	}

	protected abstract boolean isProviderMethod(Method m);
}
