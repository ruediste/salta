package com.github.ruediste.salta.standard.binder;

import net.sf.cglib.proxy.MethodProxy;

public interface SaltaMethodInterceptor {
    /**
     * Intercept an invocation of a method. Use
     * {@code proxy.invoke(delegate,args)} to forward to the delegate
     */
    public Object intercept(Object delegate, java.lang.reflect.Method method, Object[] args, MethodProxy proxy)
            throws Throwable;
}
