# SimpleDi

A dependency injection framework inspired by [Guice](https://github.com/google/guice). The configuration EDSL is copied almost 1-1, but the inner workings are completely different.

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Design Overview
SimpleDI was created as a response to shortcomings of both (JavaEE CDI)[http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html] and (Guice)[https://github.com/google/guice]. 

In CDI, the set of available beans (the counterpart of bindings in Guice) is determined when the container is initialized and cannot be changed afterwards. This implies that the available classes are scanned during startup, which results in slow startup speeds.

In Guice, the bindings can be created while the container is running. However, the focus lies on robustness. There are some major shortcomings in the area of flexibility. The annotations are hardcoded and there is no way to add a standard dependency (annotated with @Inject) to an injection point specific instance. (As it would for example be necessary to nicely integrate slf4j loggers)

So the goal for SimpleDI is to provide an API close to Guice, while focusing on fexibility. No class scanning happens at startup. Bindings can be created just in time, with the possiblity to specify explicit bindings in Modules. Depedencies can be satisfied in an injection point specific way.

## Bindings
Bindings are a central element of SimpleDI. 

First, if a binding itself is requested again by a dependency while resolving the binding, we call it a circular dependency. A circular dependency is resolved by satisfying the recursive request with a circular proxy, which is late bound to the final instance.  

Second, bindings are used as keys when applying a scope. Based on the binding, the scope determines whether to return a previously created instance or a new one.

In the following paragraphs, various properties of bindings will be derived from design goals concerning the robustness of application built with SimpleDI.

Consider a singleton which is injected into two different classes A and B. If the instance created by the binding would depend on the injection point, it would make a difference whether A or B is created first. Therefore the instances created by bindings may not depend on the injection point.

If no binding is found for an injection point, it is attempted to create a just in time (JIT) binding. The JIT binding may never be used instead of any existing binding. Otherwise, an injection point bound to a certain binding would suddenly be bound to the JIT binding once it is used. 

Consider an interface I and two classes A and B implementing the interface. If a JIT binding for A or B also matches I, it would make a difference whether A or B are used first when injecting I later. Therefore, we allow JIT bindings only to match the type requested at an injection point. For symmetry reason, the same applies to statically created bindings.