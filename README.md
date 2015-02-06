# SimpleDi

A dependency injection framework inspired by [Guice](https://github.com/google/guice). The configuration EDSL is copied almost 1-1, but the inner workings are completely different.

** PASSES JSR330 (javax.inject) Technology Compatibility Kit (TCK) **

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Design Overview
SimpleDI was created as a response to shortcomings of both (JavaEE CDI)[http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html] and (Guice)[https://github.com/google/guice]. 

In CDI, the set of available beans (the counterpart of bindings in Guice) is determined when the container is initialized and cannot be changed afterwards. This implies that the available classes are scanned during startup, which results in slow startup speeds.

In Guice, the bindings can be created while the container is running. However, the focus lies on robustness. There are some major shortcomings in the area of flexibility. The annotations are hardcoded and there is no way to add a standard dependency (annotated with @Inject) to an injection point specific instance. (As it would for example be necessary to nicely integrate slf4j loggers)

So the goal for SimpleDI is to provide an API close to Guice, while focusing on fexibility. No class scanning happens at startup. Bindings can be created just in time, with the possiblity to specify explicit bindings in Modules. Depedencies can be satisfied in an injection point specific way.

## Bindings
Bindings are a central element of SimpleDI. 

First, if a binding itself is requested again by a dependency while resolving the binding, we call it a circular dependency. A circular dependency is resolved by satisfying the recursive request with a circular proxy, which is later bound to the final instance.  

Second, bindings are used as keys when applying a scope. Based on the binding, the scope determines whether to return a previously created instance or a new one.

In the following paragraphs, various properties of bindings will be derived from design goals concerning the robustness of application built with SimpleDI.

Each binding B is used to satisfy a set of injection points IP(B). Due to the scope, an instance created for one injection point can be used for any other other injection point in IP(B). If the instance created by B would depend on the actual injection point, a different instance would be used depending on which injection point is triggered first. For example consider a singleton which is injected into two different classes A and B. If the instance created by the binding would depend on the injection point, it would make a difference whether A or B is created first. 

Therefore the instances created by bindings may not depend on the injection point.

If multiple bindings match for a single injection point, we can not just arbitrarily choose one binding. Either we have to define a precedence among the bindings or we fail. SimpleDI fails in this situation. 

If no binding is found for an injection point, it is attempted to create a just in time (JIT) binding. The JIT binding may never be used instead of any existing binding. Otherwise, an injection point bound to a certain binding would suddenly be bound to the JIT binding once the JIT binding is created.

Depending on the input data or requests of an application, the order in which JIT bindings are requested and thus created varies highly. For the robustness of the application it is important that the bindings used for an injection point does not vary on this order.

As long as no injection point can be served by more than one JIT binding, this is not an issue. However, as soon as the sets of injection points served by the JIT bindings overlaps, we are in trouble.  

Consider a new JIT binding B which is getting created. To make sure no injection would have been bound to B if B would have been present already at the time the injection, all we have to check if any of the already injected injection points matches B. If any matches, we detected an error. We cannot let B beeing created. However, depending on the JIT binding creation order, different bindings can reveal the conflicts, or the conflict can not be revealed at all.

This behaviour is clearly not an option. Therefore we split the injection points for JIT bindings into non-overlapping regions.

This is achieved by creating a key from the injection point. A JIT binding is only used to satisfy injection points with a key equal to the one used to create the binding.