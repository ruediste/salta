package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.CompiledFunction;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.google.common.reflect.TypeToken;

public class MembersInjectorImpl<T> implements MembersInjector<T> {

	private StandardInjector injector;
	private TypeToken<?> type;

	public MembersInjectorImpl(TypeToken<?> type, Injector injector,
			StandardInjector standardInjector) {
		this.type = type;
		this.injector = standardInjector;
	}

	private volatile CompiledFunction injectionFunction;

	@Override
	public void injectMembers(T instance) {
		if (injectionFunction == null) {
			synchronized (injector.getCoreInjector().recipeLock) {
				if (injectionFunction == null) {
					RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
							injector.getCoreInjector());
					FunctionRecipe recipe = injector.getMembersInjectionRecipe(
							type, ctx);
					ctx.processQueuedActions();
					injectionFunction = injector.getCoreInjector()
							.compileFunction(recipe);
				}
			}
		}
		try {
			injectionFunction.get(instance);
		} catch (Throwable e) {
			throw new SaltaException(
					"Error while injecting members of instance of " + type, e);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "MembersInjector<" + type + ">";
	}
}
