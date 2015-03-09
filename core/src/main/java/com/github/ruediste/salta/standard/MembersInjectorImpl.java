package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.CompiledFunction;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.google.common.reflect.TypeToken;

public class MembersInjectorImpl<T> implements MembersInjector<T> {

	private CoreInjector coreInjector;
	private Injector injector;
	private TypeToken<?> type;

	public MembersInjectorImpl(TypeToken<?> type, Injector injector,
			CoreInjector coreInjector) {
		this.type = type;
		this.injector = injector;
		this.coreInjector = coreInjector;
	}

	private volatile CompiledFunction injectionFunction;

	@Override
	public void injectMembers(T instance) {
		if (injectionFunction == null) {
			synchronized (coreInjector.recipeLock) {
				if (injectionFunction == null) {
					RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
							coreInjector);
					FunctionRecipe recipe = injector.getMembersInjectionRecipe(
							type, ctx);
					ctx.processQueuedActions();
					injectionFunction = coreInjector.compileFunction(recipe);
				}
			}
		}
		try {
			injectionFunction.get(instance);
		} catch (Throwable e) {
			throw new SaltaException(
					"Error while injecting members of instance of " + type
							+ "\n" + e.getMessage(), e);
		}
	}
}
