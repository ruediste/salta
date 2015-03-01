# Salta Dependency Injection Framework

 * Familiar: API very similar to Guice
 * Fast: more than 10 times faster than Guice
 * Flexible: customize API, Annotations, Behaviour ...
 
A dependency injection framework inspired by [Guice](https://github.com/google/guice). The configuration EDSL is copied almost 1-1, but the inner workings are completely different.

** PASSES JSR330 (javax.inject) Technology Compatibility Kit (TCK) **

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Design Overview
Salta was created as a response to shortcomings of (JavaEE CDI)[http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html], (Guice)[https://github.com/google/guice] and Spring.

In CDI, the set of available beans (the counterpart of bindings in Guice) is determined when the container is initialized and cannot be changed afterwards. This implies that the available classes are scanned during startup, which results in slow startup for large applications.

In Guice, the bindings can be created while the container is running (JIT bindings). However, the focus lies on robustness. There are some major shortcomings in the area of flexibility. The annotations are hardcoded and there is no way to add a standard dependency (annotated with @Inject) to an injection point specific instance. (As it would for example be necessary to nicely integrate slf4j loggers)

Spring is very slow.

So the goal for SimpleDI is to provide an API close to Guice, while focusing on fexibility. No class scanning happens at startup. Bindings can be created just in time, with the possibility to specify explicit bindings in Modules. Depedencies can be satisfied in an injection point specific way. In addition, creating and injecting instances is almost as fast as hand written code due to bytecode generation.

## Locking
Salta tries to do as much as possible without locking. If synchronization is required, there is one lock for the creation of recipes (recipe lock) and one for the creation of instances (instantiation lock). The recipe lock is acquired eagerly, whenever a new recipe needs to be created. The instantiation lock is mainly used if a scope needs to make sure only a single instance is created for a binding.

To avoid deadlocks, a thread holding the instantiation lock may not acquire the recipe lock. Thus all code in the compiled creation recipe, all constructors, injected methods and post construct methods may not use Injector.getInstance(). 

## Speed
Salta uses bytecode generation to speed up instantiation. Expect a 5x to 10x speedup over Guice.

### Bechmark Results
We use different workloads to test the performance of Salta and compare it to Guice. Code generation is used to create large class graphs. All benchmarks are executed on a 
**Intel(R) Core(TM) i7-3820 CPU @ 3.60GHz** with 16GB RAM, running Ubuntu Linux.

In the TREE workload, each class depends on 10 other classes, except for the leaf classes. The depth of the tree is varied. The number of classes in the tree is

 * 1 for depth 0 
 * 11 for depth 1 
 * 111 for depth 2
 * 1111 for depth 3 
 * 11111 for depth 4 

The dependencies are injected using constructors, methods or fields. Also, the visibility of the constructors, members or field is varied  between public, package, protected and private.

Results for depth 4:
	
	Benchmark                (injection)  (visibility)   Mode  Cnt     Score      Error   Units
	GuiceThroughput.measure       METHOD        PUBLIC  thrpt   10     0.066 ±    0.009  ops/ms
	GuiceThroughput.measure       METHOD       PACKAGE  thrpt   10     0.061 ±    0.012  ops/ms
	GuiceThroughput.measure       METHOD     PROTECTED  thrpt   10     0.050 ±    0.001  ops/ms
	GuiceThroughput.measure       METHOD       PRIVATE  thrpt   10     0.050 ±    0.001  ops/ms
	GuiceThroughput.measure  CONSTRUCTOR        PUBLIC  thrpt   10     0.098 ±    0.004  ops/ms
	GuiceThroughput.measure  CONSTRUCTOR       PACKAGE  thrpt   10     0.089 ±    0.003  ops/ms
	GuiceThroughput.measure  CONSTRUCTOR     PROTECTED  thrpt   10     0.083 ±    0.017  ops/ms
	GuiceThroughput.measure  CONSTRUCTOR       PRIVATE  thrpt   10     0.092 ±    0.002  ops/ms
	GuiceThroughput.measure        FIELD        PUBLIC  thrpt   10     0.082 ±    0.002  ops/ms
	GuiceThroughput.measure        FIELD       PACKAGE  thrpt   10     0.083 ±    0.002  ops/ms
	GuiceThroughput.measure        FIELD     PROTECTED  thrpt   10     0.084 ±    0.006  ops/ms
	GuiceThroughput.measure        FIELD       PRIVATE  thrpt   10     0.084 ±    0.003  ops/ms
	GuiceStartup.measure          METHOD        PUBLIC     ss   10  8442.462 ±  439.050   ms/op
	GuiceStartup.measure          METHOD       PACKAGE     ss   10  8079.176 ±  499.078   ms/op
	GuiceStartup.measure          METHOD     PROTECTED     ss   10  7821.022 ±  258.310   ms/op
	GuiceStartup.measure          METHOD       PRIVATE     ss   10  7914.901 ±  513.936   ms/op
	GuiceStartup.measure     CONSTRUCTOR        PUBLIC     ss   10  7641.369 ±  538.386   ms/op
	GuiceStartup.measure     CONSTRUCTOR       PACKAGE     ss   10  7060.088 ±  533.836   ms/op
	GuiceStartup.measure     CONSTRUCTOR     PROTECTED     ss   10  7327.997 ±  817.067   ms/op
	GuiceStartup.measure     CONSTRUCTOR       PRIVATE     ss   10  6729.250 ±  306.703   ms/op
	GuiceStartup.measure           FIELD        PUBLIC     ss   10  7710.462 ± 1505.247   ms/op
	GuiceStartup.measure           FIELD       PACKAGE     ss   10  8589.887 ± 1938.777   ms/op
	GuiceStartup.measure           FIELD     PROTECTED     ss   10  7470.670 ±  251.739   ms/op
	GuiceStartup.measure           FIELD       PRIVATE     ss   10  7216.927 ±  315.699   ms/op


## Bindings
Bindings are a central element of SimpleDI. 

First, if a binding itself is requested again by a dependency while resolving the binding, we call it a circular dependency. We do not allow circular dependencies due to predictability issues. See further down. 

Second, bindings are used as keys when applying a scope. Based on the binding, the scope determines whether to return a previously created instance or a new one.

We want the dependencies injected to be predictable. In the following paragraphs, various properties of bindings will be derived from this goal.

Each binding B is used to satisfy a set of injection points IP(B). Due to the scope, an instance created for one injection point can be used for any other other injection point in IP(B). If the instance created by B would depend on the actual injection point, a different instance would be used depending on which injection point is triggered first. For example consider a singleton which is injected into two different classes A and B. If the instance created by the binding would depend on the injection point, it would make a difference whether A or B is created first. 

Therefore the instances created by bindings may not depend on the injection point.

If multiple bindings match for a single injection point, we can not just arbitrarily choose one binding. Either we have to define a precedence among the bindings or we fail. SimpleDI fails in this situation. 

If no binding is found for an injection point, it is attempted to create a just in time (JIT) binding. The JIT binding may never be used instead of any existing binding. Otherwise, an injection point bound to a certain binding would suddenly be bound to the JIT binding once the JIT binding is created.

Depending on the input data or requests of an application, the order in which JIT bindings are requested and thus created varies highly. For the predictability of the dependency injection it is important that the bindings used for an injection point does not vary on this order.

As long as no injection point can be served by more than one JIT binding, this is not an issue. However, as soon as the sets of injection points served by the JIT bindings overlaps, we are in trouble.  

Consider a new JIT binding B which is getting created. To make sure no injection would have been bound to B if B would have been present already at the time the injection, all we have to check if any of the already injected injection points matches B. If any matches, we detected an error. We cannot let B beeing created. However, depending on the JIT binding creation order, different bindings can reveal the conflicts, or the conflict can not be revealed at all.

This behavior is clearly not an option. Therefore we split the injection points for JIT bindings into non-overlapping regions.

This is achieved by creating a key from the injection point. A JIT binding is only used to satisfy injection points with a key equal to the one used to create the binding.

## Circular Dependencies
Consider a class A with a constructor parameter of type B, and a class B with a constructor parameter of type A. 

If A is requested, a circular proxy of A could be used to create the instance of B. But if B accesses the proxy from within the constructor, expecting a fully initialized instance of A, an exception will be thrown.

In the same setup, given A does not access the passed instance of B in the constructor, B can be created. If B is a singleton as well, if the application first requests a B it will run happily, even using instances of A. If it first requests an instance of A an error will occur.

This is certainly not a stable and predictable behavior. Therefore Salta does not support circular dependencies.