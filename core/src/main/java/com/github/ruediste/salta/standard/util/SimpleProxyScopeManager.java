package com.github.ruediste.salta.standard.util;

import java.util.Map;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Scopes a single execution of a block of code. In contrast to
 * {@link SimpleScopeManager}, this scope handler creates a proxy, which will
 * always delegate to the instance in the current scope. Apply this scope with a
 * try/finally block:
 *
 * <pre>
 * <code>
 *
 *   scopeHandler.enter();
 *   try {
 *     // create and access scoped objects
 *   } finally {
 *     scopeHandler.exit();
 *   }
 * </code>
 * </pre>
 *
 * Register it with
 *
 * <pre>
 * <code>
 * SimpleProxyScopeHandler scopeHandler=new SimpleProxyScopeHandler();
 * bindScope(MyCustomScopeAnnotation.class, new ScopeImpl(scopeHandler));
 * bind(SimpleProxyScopeHandler.class).named("myScope").toInstance(scopeHandler);
 * </code>
 * </pre>
 *
 * @author Jesse Wilson
 * @author Fedor Karpelevitch
 * @author Ruedi Steinmann
 */
public class SimpleProxyScopeManager extends SimpleScopeManagerBase {

	private class ScopedObjectSupplier implements Supplier<Object> {
		private Binding binding;
		private CoreDependencyKey<?> requestedKey;
		private Supplier<Object> supplier;

		public ScopedObjectSupplier(Supplier<Object> supplier, CoreDependencyKey<?> requestedKey, Binding binding) {
			this.supplier = supplier;
			this.requestedKey = requestedKey;
			this.binding = binding;
		}

		@Override
		public Object get() {
			Map<Binding, Object> scopedObjects = tryGetValueMap().orElseThrow(
					() -> new RuntimeException("Cannot access " + requestedKey + " outside of scope " + scopeName));
			return scopedObjects.computeIfAbsent(binding, b -> supplier.get());
		}
	}

	public SimpleProxyScopeManager(String scopeName) {
		super(scopeName);
	}

	@Override
	public Supplier<Object> scope(Supplier<Object> supplier, Binding binding, CoreDependencyKey<?> requestedKey) {

		ScopedObjectSupplier objSupplier = new ScopedObjectSupplier(supplier, requestedKey, binding);

		// create the proxy right away, such that it can be reused
		// afterwards
		Object proxy;
		try {
			proxy = new ByteBuddy().subclass(requestedKey.getRawType())
					.method(ElementMatchers.not(ElementMatchers.isStatic().or(ElementMatchers.isNative())))

					.intercept(MethodCall.invokeSelf()
							.onMethodCall(
									MethodCall.invoke(Supplier.class.getMethod("get")).on(objSupplier, Supplier.class))
							.withAllArguments().withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))

					.make()
					.load(requestedKey.getRawType().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent())
					.getLoaded().getConstructor().newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error while creating proxy", e);
		}

		return () -> proxy;
	}

}