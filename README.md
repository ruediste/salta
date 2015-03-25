# Salta Dependency Injection Framework

 * Familiar: same configuration API as Guice, similar concepts
 * Fast: fast startup, fast object instantiation
 * Flexible: customize API, annotations, behaviour ...
 
** PASSES JSR330 (javax.inject) Technology Compatibility Kit (TCK) **

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Familiar
To make things easy for developers, the configuration API of Salta was copied from Guice (without the SPI). The concepts of modules, bindings, JIT bindings and scopes was taken over as well. Thus, if you know Guice, the learning curve is very gentle.

The only truly new concept added is the Creation Rule, used to construct injection point specific instances.

## Fast
To start big applications fast, Salta avoids processing all available classes during startup. Instead, bindings are constructed just in time (JIT) as they are needed. The creation of JIT bindings is fully customizable.

Care has been taken to make sure the order in which the JIT bindings are constructed can not affect the outcome. Otherwise, different runs of your program would lead to different objects beeing injected.

To instantiate objects fast, Salta relies heavily on bytecode generation, resulting in a speedup between 10x and 30x over Guice. 

Bytecode generation does not lead to slow startup speed. Salta is consistently slightly faster than Guice.

## Flexible
Salta has been designed from ground up to be flexible:

 * customize JIT bindings
 * customize injection points
 * no fixed annotations
 * injection point specific instances
 * custom scopes

Read the design overview for details

## Motivation

While developing a full stack web framework, we were looking for a dependency injection framework allowing for a quick application restart, even for large applications. 

This ruled out (JavaEE CDI)[http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html] since the set of available beans (the counterpart of bindings in Guice) is determined when the container is initialized and cannot be changed afterwards. This implies that the available classes are scanned during startup, which results in slow startup for large applications.

In (Guice)[https://github.com/google/guice], the bindings can be created while the container is running (JIT bindings). However there is no way to add a standard dependency (annotated with @Inject) to an injection point specific instance. (As it would for example be necessary to nicely integrate slf4j loggers). Also, the JIT binding rules are fixed and the framework already brings two sets of annotations to the classpath (com.google.inject.* and javax.inject.*)

Spring is known to be very slow.

For these reasons, we started the Salta project.

## Design
Salta is split into a core module, a Standard module and multiple API modules.

### Core Module
The Core module defines the central concepts of the injection framework. It ensures predictable injection and provides the compilation infrastructure. There is no configuration DSL.

 * **Core Dependency Key:**
A dependency key represents a dependency which should be constructed. This can be an injection point (constructor or method parameter, field) or a programmatic lookup (Injector.getInstance(...)). 

 * **Creation Rules:**
Creation Rules allow to create dependency key specific instances. The created instances can not take part in scoping.

 * **Static Bindings:**
If no creation rule has been found for a key, the static bindings are evaluated. First, each binding is matched against the key. If multiple bindings match, and error is raised. If a single binding is found, it is asked to create an instance. At this time the binding does not have access to the key anymore, thus the created instance can not be injection point specific.

 * **JIT Binding Key:**
If no static binding is found, a JIT binding key is constructed from the dependency key using the JIT binding key rules. This key is then matched against the JIT binding rules. The first matching rule creates a JIT binding. The JIT binding is stored using the JIT binding key and used if the same key is requested afterwards.

    This scheme for JIT bindings is somewhat complicated, but it enables to achieve predictable bindings while allowing to share JIT bindings between different injection points:

    It is trivially clear that the JIT binding beeing constructed does not depend on the JIT bindings already created. They are simply not used in the algorithm. Sharing between injection points is possible by making sure the injection points map to the same JIT binding key.

 * **Recipes and Compilation:**
Creation rules, static bindings and JIT bindings all produce recipes which are then compiled to byte code. 

 * **Locking:**
Salta uses coarse grained locking to control concurrency. The recipe lock is acquired whenever a new dependency key is processed and held during binding matching, recipe creation and compilation. It should be used as well if scopes need synchronization.

 * **Circular Dependencies:**
Salta does not support circular dependencies. There is simply no way to make them work in a predictable way. Where should cycles be broken? If you encounter a circular dependency, use a provider to resolve it.

### Standard Module
The standard module contains common code to implement a guice-like dependency injection framework on top of the core module. It provides fine grained customization points and organizes the cooperation between different modules. 

Care has been taken not to include any API components typically used directly by an application. Providing the API is the responsibility of the API modules.

 * **Instance Creation:** The standard module splits instance creation into the following steps: instantiation, members injection, initialization, enhancement, scoping. Each of these aspects can be configured using rules or factories in the *StandardInjectorConfiguration**. This allows for a very fine grained customization of each of these aspects.
  
 * **Scopes:** Scopes are used to reuse a single instance multiple times, or to inject a different instance in different threads. Only instances provided by bindings can be scoped, since the binding is used to identify the instance to be injected. Scoping happens after the instance creation
 
 * **Modules:** Modules group bindings and configuration options together. They are used to configure salta.
 
 * **Binding DSL:** An implementation of the Guice binding DSL is provided using the standard JRE functional interfaces. This implementation is typically wrapped by the API module for easy API implementation.
 
### JSR330 Module
This module provides a bare bone API based on the JSR330 javax.inject annotations and types, combined with a Guice like binding DSL.

### Guice Module
This module serves as almost drop-in replacement for Guice.

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

Results for depth 2:

	Benchmark             (injection)  (injectionStrategy)  (visibility)   Mode  Cnt     Score     Error   Units
	GuiceThroughput.bind       METHOD                  N/A        PUBLIC  thrpt   10    53.184 ±   6.731  ops/ms
	GuiceThroughput.bind       METHOD                  N/A       PACKAGE  thrpt   10    57.722 ±  11.749  ops/ms
	GuiceThroughput.bind       METHOD                  N/A     PROTECTED  thrpt   10    42.947 ±   7.769  ops/ms
	GuiceThroughput.bind       METHOD                  N/A       PRIVATE  thrpt   10    42.335 ±   1.574  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A        PUBLIC  thrpt   10    92.924 ±   5.277  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A       PACKAGE  thrpt   10    90.212 ±   5.215  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A     PROTECTED  thrpt   10    88.682 ±   7.160  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A       PRIVATE  thrpt   10    88.897 ±   7.693  ops/ms
	GuiceThroughput.bind        FIELD                  N/A        PUBLIC  thrpt   10    77.193 ±   3.523  ops/ms
	GuiceThroughput.bind        FIELD                  N/A       PACKAGE  thrpt   10    78.591 ±   4.018  ops/ms
	GuiceThroughput.bind        FIELD                  N/A     PROTECTED  thrpt   10    75.793 ±   4.034  ops/ms
	GuiceThroughput.bind        FIELD                  N/A       PRIVATE  thrpt   10    73.640 ±   4.110  ops/ms
	GuiceThroughput.jit        METHOD                  N/A        PUBLIC  thrpt   10    54.414 ±   2.358  ops/ms
	GuiceThroughput.jit        METHOD                  N/A       PACKAGE  thrpt   10    57.505 ±   3.006  ops/ms
	GuiceThroughput.jit        METHOD                  N/A     PROTECTED  thrpt   10    49.273 ±   1.467  ops/ms
	GuiceThroughput.jit        METHOD                  N/A       PRIVATE  thrpt   10    47.958 ±   1.929  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A        PUBLIC  thrpt   10    93.431 ±   4.727  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A       PACKAGE  thrpt   10    89.629 ±   4.934  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A     PROTECTED  thrpt   10    91.943 ±   4.721  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A       PRIVATE  thrpt   10    93.124 ±   5.506  ops/ms
	GuiceThroughput.jit         FIELD                  N/A        PUBLIC  thrpt   10    70.436 ±   4.835  ops/ms
	GuiceThroughput.jit         FIELD                  N/A       PACKAGE  thrpt   10    70.497 ±   4.924  ops/ms
	GuiceThroughput.jit         FIELD                  N/A     PROTECTED  thrpt   10    76.393 ±   4.242  ops/ms
	GuiceThroughput.jit         FIELD                  N/A       PRIVATE  thrpt   10    78.330 ±   3.377  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC        PUBLIC  thrpt   10  3048.985 ± 100.539  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC       PACKAGE  thrpt   10   903.226 ±  64.969  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC     PROTECTED  thrpt   10  1042.216 ±  94.632  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3012.583 ±  87.397  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC  thrpt   10  2848.874 ± 117.853  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE  thrpt   10  3029.303 ±  18.151  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED  thrpt   10  3028.243 ±  57.548  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3025.744 ±  98.660  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC        PUBLIC  thrpt   10  3035.663 ±  64.943  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC       PACKAGE  thrpt   10  3054.545 ±  53.454  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC     PROTECTED  thrpt   10  2981.779 ± 130.167  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3059.706 ±  55.114  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC        PUBLIC  thrpt   10  3049.825 ±  81.149  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC       PACKAGE  thrpt   10  1128.279 ±  33.087  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC     PROTECTED  thrpt   10  1184.837 ±  34.339  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3075.690 ±  59.745  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC  thrpt   10  2839.472 ±  80.464  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE  thrpt   10  2998.377 ± 163.368  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED  thrpt   10  3042.142 ±  69.207  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3002.486 ± 110.543  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC        PUBLIC  thrpt   10  3080.668 ±  63.787  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC       PACKAGE  thrpt   10  3015.873 ±  71.816  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC     PROTECTED  thrpt   10  3037.711 ± 106.141  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC       PRIVATE  thrpt   10  3003.920 ±  93.435  ops/ms
	GuiceStartup.bind          METHOD                  N/A        PUBLIC     ss   10   358.216 ±  23.827   ms/op
	GuiceStartup.bind          METHOD                  N/A       PACKAGE     ss   10   359.387 ±  40.800   ms/op
	GuiceStartup.bind          METHOD                  N/A     PROTECTED     ss   10   348.555 ±  40.878   ms/op
	GuiceStartup.bind          METHOD                  N/A       PRIVATE     ss   10   367.173 ±  50.680   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A        PUBLIC     ss   10   351.889 ±  62.540   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A       PACKAGE     ss   10   352.236 ±  48.189   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A     PROTECTED     ss   10   343.481 ±  40.639   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A       PRIVATE     ss   10   335.248 ±  36.823   ms/op
	GuiceStartup.bind           FIELD                  N/A        PUBLIC     ss   10   338.567 ±  20.255   ms/op
	GuiceStartup.bind           FIELD                  N/A       PACKAGE     ss   10   336.423 ±  16.747   ms/op
	GuiceStartup.bind           FIELD                  N/A     PROTECTED     ss   10   339.697 ±  20.447   ms/op
	GuiceStartup.bind           FIELD                  N/A       PRIVATE     ss   10   337.982 ±  19.924   ms/op
	GuiceStartup.jit           METHOD                  N/A        PUBLIC     ss   10   334.513 ±  46.532   ms/op
	GuiceStartup.jit           METHOD                  N/A       PACKAGE     ss   10   317.212 ±  15.614   ms/op
	GuiceStartup.jit           METHOD                  N/A     PROTECTED     ss   10   309.336 ±   5.360   ms/op
	GuiceStartup.jit           METHOD                  N/A       PRIVATE     ss   10   341.251 ±  51.933   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A        PUBLIC     ss   10   305.092 ±  19.580   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A       PACKAGE     ss   10   301.760 ±  23.552   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A     PROTECTED     ss   10   307.548 ±  39.367   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A       PRIVATE     ss   10   302.733 ±  33.245   ms/op
	GuiceStartup.jit            FIELD                  N/A        PUBLIC     ss   10   316.570 ±  35.833   ms/op
	GuiceStartup.jit            FIELD                  N/A       PACKAGE     ss   10   316.428 ±  36.766   ms/op
	GuiceStartup.jit            FIELD                  N/A     PROTECTED     ss   10   315.988 ±  22.807   ms/op
	GuiceStartup.jit            FIELD                  N/A       PRIVATE     ss   10   312.055 ±  16.903   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC        PUBLIC     ss   10   244.098 ±   4.140   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC       PACKAGE     ss   10   267.972 ±   4.982   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC     PROTECTED     ss   10   282.769 ±  47.659   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC       PRIVATE     ss   10   274.330 ±  28.069   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC     ss   10   253.096 ±  42.339   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE     ss   10   253.762 ±  27.383   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED     ss   10   249.430 ±  29.511   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE     ss   10   244.278 ±  14.867   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC        PUBLIC     ss   10   250.114 ±  31.275   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC       PACKAGE     ss   10   280.167 ±  24.375   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC     PROTECTED     ss   10   272.376 ±  11.333   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC       PRIVATE     ss   10   282.349 ±  33.898   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC        PUBLIC     ss   10   222.079 ±  34.384   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC       PACKAGE     ss   10   243.439 ±  15.716   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC     PROTECTED     ss   10   249.619 ±  39.985   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC       PRIVATE     ss   10   237.482 ±   3.935   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC     ss   10   206.111 ±  10.626   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE     ss   10   217.669 ±  34.528   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED     ss   10   223.562 ±  29.698   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE     ss   10   212.704 ±   9.092   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC        PUBLIC     ss   10   212.101 ±   3.093   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC       PACKAGE     ss   10   240.993 ±  10.611   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC     PROTECTED     ss   10   241.824 ±  14.351   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC       PRIVATE     ss   10   237.888 ±   2.738   ms/op


Results for depth 3:

	Benchmark             (injection)  (injectionStrategy)  (visibility)   Mode  Cnt     Score     Error   Units
	GuiceThroughput.bind       METHOD                  N/A        PUBLIC  thrpt   10     1.441 ±   0.830  ops/ms
	GuiceThroughput.bind       METHOD                  N/A       PACKAGE  thrpt   10     1.079 ±   0.553  ops/ms
	GuiceThroughput.bind       METHOD                  N/A     PROTECTED  thrpt   10     0.761 ±   0.308  ops/ms
	GuiceThroughput.bind       METHOD                  N/A       PRIVATE  thrpt   10     0.584 ±   0.169  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A        PUBLIC  thrpt   10     2.027 ±   1.685  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A       PACKAGE  thrpt   10     3.441 ±   0.851  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A     PROTECTED  thrpt   10     3.290 ±   0.953  ops/ms
	GuiceThroughput.bind  CONSTRUCTOR                  N/A       PRIVATE  thrpt   10     3.745 ±   0.802  ops/ms
	GuiceThroughput.bind        FIELD                  N/A        PUBLIC  thrpt   10     4.323 ±   0.323  ops/ms
	GuiceThroughput.bind        FIELD                  N/A       PACKAGE  thrpt   10     3.313 ±   1.805  ops/ms
	GuiceThroughput.bind        FIELD                  N/A     PROTECTED  thrpt   10     2.067 ±   2.185  ops/ms
	GuiceThroughput.bind        FIELD                  N/A       PRIVATE  thrpt   10     3.440 ±   1.514  ops/ms
	GuiceThroughput.jit        METHOD                  N/A        PUBLIC  thrpt   10     2.632 ±   1.268  ops/ms
	GuiceThroughput.jit        METHOD                  N/A       PACKAGE  thrpt   10     1.924 ±   1.143  ops/ms
	GuiceThroughput.jit        METHOD                  N/A     PROTECTED  thrpt   10     0.656 ±   0.058  ops/ms
	GuiceThroughput.jit        METHOD                  N/A       PRIVATE  thrpt   10     0.601 ±   0.042  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A        PUBLIC  thrpt   10     4.134 ±   0.252  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A       PACKAGE  thrpt   10     3.933 ±   0.606  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A     PROTECTED  thrpt   10     4.870 ±   0.520  ops/ms
	GuiceThroughput.jit   CONSTRUCTOR                  N/A       PRIVATE  thrpt   10     4.429 ±   0.359  ops/ms
	GuiceThroughput.jit         FIELD                  N/A        PUBLIC  thrpt   10     4.470 ±   0.145  ops/ms
	GuiceThroughput.jit         FIELD                  N/A       PACKAGE  thrpt   10     4.539 ±   0.265  ops/ms
	GuiceThroughput.jit         FIELD                  N/A     PROTECTED  thrpt   10     4.536 ±   0.213  ops/ms
	GuiceThroughput.jit         FIELD                  N/A       PRIVATE  thrpt   10     4.620 ±   0.137  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC        PUBLIC  thrpt   10   112.334 ±   6.029  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC       PACKAGE  thrpt   10    25.024 ±   1.506  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC     PROTECTED  thrpt   10    24.583 ±   1.572  ops/ms
	SaltaThroughput.bind       METHOD       INVOKE_DYNAMIC       PRIVATE  thrpt   10   112.376 ±  16.587  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC  thrpt   10   109.658 ±  10.594  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE  thrpt   10    16.563 ±   0.801  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED  thrpt   10    16.761 ±   0.989  ops/ms
	SaltaThroughput.bind  CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE  thrpt   10    16.653 ±   1.187  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC        PUBLIC  thrpt   10   115.231 ±   8.711  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC       PACKAGE  thrpt   10   104.815 ±   8.318  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC     PROTECTED  thrpt   10    98.038 ±  15.843  ops/ms
	SaltaThroughput.bind        FIELD       INVOKE_DYNAMIC       PRIVATE  thrpt   10   105.113 ±  12.261  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC        PUBLIC  thrpt   10   116.963 ±   6.660  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC       PACKAGE  thrpt   10    24.803 ±   1.447  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC     PROTECTED  thrpt   10    24.589 ±   1.893  ops/ms
	SaltaThroughput.jit        METHOD       INVOKE_DYNAMIC       PRIVATE  thrpt   10   113.449 ±  11.013  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC  thrpt   10   114.136 ±  17.200  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE  thrpt   10    16.312 ±   1.363  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED  thrpt   10    16.584 ±   0.909  ops/ms
	SaltaThroughput.jit   CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE  thrpt   10    16.474 ±   1.909  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC        PUBLIC  thrpt   10   116.995 ±   9.138  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC       PACKAGE  thrpt   10   103.238 ±  12.611  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC     PROTECTED  thrpt   10   101.769 ±  11.408  ops/ms
	SaltaThroughput.jit         FIELD       INVOKE_DYNAMIC       PRIVATE  thrpt   10   101.720 ±  11.210  ops/ms
	GuiceStartup.bind          METHOD                  N/A        PUBLIC     ss   10  1424.984 ± 199.287   ms/op
	GuiceStartup.bind          METHOD                  N/A       PACKAGE     ss   10  1408.609 ± 156.808   ms/op
	GuiceStartup.bind          METHOD                  N/A     PROTECTED     ss   10  1348.665 ± 156.429   ms/op
	GuiceStartup.bind          METHOD                  N/A       PRIVATE     ss   10  1378.556 ± 222.143   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A        PUBLIC     ss   10  1293.249 ± 118.633   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A       PACKAGE     ss   10  1229.740 ± 129.589   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A     PROTECTED     ss   10  1226.714 ± 149.464   ms/op
	GuiceStartup.bind     CONSTRUCTOR                  N/A       PRIVATE     ss   10  1245.763 ± 148.923   ms/op
	GuiceStartup.bind           FIELD                  N/A        PUBLIC     ss   10  1323.059 ± 181.396   ms/op
	GuiceStartup.bind           FIELD                  N/A       PACKAGE     ss   10  1324.281 ± 152.359   ms/op
	GuiceStartup.bind           FIELD                  N/A     PROTECTED     ss   10  1313.831 ± 181.921   ms/op
	GuiceStartup.bind           FIELD                  N/A       PRIVATE     ss   10  1306.492 ± 136.248   ms/op
	GuiceStartup.jit           METHOD                  N/A        PUBLIC     ss   10  1195.295 ± 149.547   ms/op
	GuiceStartup.jit           METHOD                  N/A       PACKAGE     ss   10  1181.128 ± 150.931   ms/op
	GuiceStartup.jit           METHOD                  N/A     PROTECTED     ss   10  1150.795 ± 138.435   ms/op
	GuiceStartup.jit           METHOD                  N/A       PRIVATE     ss   10  1123.502 ± 147.460   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A        PUBLIC     ss   10  1156.548 ± 189.550   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A       PACKAGE     ss   10  1036.281 ± 129.667   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A     PROTECTED     ss   10  1051.352 ± 207.313   ms/op
	GuiceStartup.jit      CONSTRUCTOR                  N/A       PRIVATE     ss   10  1019.603 ± 122.556   ms/op
	GuiceStartup.jit            FIELD                  N/A        PUBLIC     ss   10  1138.938 ± 144.106   ms/op
	GuiceStartup.jit            FIELD                  N/A       PACKAGE     ss   10  1207.385 ± 170.160   ms/op
	GuiceStartup.jit            FIELD                  N/A     PROTECTED     ss   10  1170.451 ± 185.066   ms/op
	GuiceStartup.jit            FIELD                  N/A       PRIVATE     ss   10  1079.846 ±  96.166   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC        PUBLIC     ss   10   818.334 ±  83.920   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC       PACKAGE     ss   10  1025.007 ± 128.748   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC     PROTECTED     ss   10   936.887 ±  62.735   ms/op
	SaltaStartup.bind          METHOD       INVOKE_DYNAMIC       PRIVATE     ss   10  1002.986 ± 125.853   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC     ss   10   728.705 ±  81.118   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE     ss   10   765.831 ±  63.270   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED     ss   10   764.783 ±  64.112   ms/op
	SaltaStartup.bind     CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE     ss   10   795.655 ±  74.401   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC        PUBLIC     ss   10   788.857 ±  88.863   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC       PACKAGE     ss   10   939.460 ±  77.122   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC     PROTECTED     ss   10   959.246 ±  43.071   ms/op
	SaltaStartup.bind           FIELD       INVOKE_DYNAMIC       PRIVATE     ss   10   946.486 ± 120.487   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC        PUBLIC     ss   10   626.760 ±  56.768   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC       PACKAGE     ss   10   811.133 ±  76.751   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC     PROTECTED     ss   10   834.650 ±  97.316   ms/op
	SaltaStartup.jit           METHOD       INVOKE_DYNAMIC       PRIVATE     ss   10   796.690 ±  92.045   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC        PUBLIC     ss   10   561.121 ±  61.557   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC       PACKAGE     ss   10   588.689 ±  67.639   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC     PROTECTED     ss   10   593.523 ±  44.894   ms/op
	SaltaStartup.jit      CONSTRUCTOR       INVOKE_DYNAMIC       PRIVATE     ss   10   597.534 ±  76.979   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC        PUBLIC     ss   10   610.428 ±  98.353   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC       PACKAGE     ss   10   824.820 ±  72.068   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC     PROTECTED     ss   10   754.061 ±  43.541   ms/op
	SaltaStartup.jit            FIELD       INVOKE_DYNAMIC       PRIVATE     ss   10   785.188 ± 102.295   ms/op

Results for depth 4:
	
## Creating Releases
During development, the version is always set to the next version with the -SNAPSHOT suffix.

To build a release, first the `~/.m2/settings.xml` file has to be set up using the Sonatype Jira credentials:

	<?xml version="1.0" encoding="UTF-8"?>
	<settings>
		<servers>
			<server>
				<id>ossrh</id>
				<username>your-jira-id</username>
				<password>your-jira-pwd</password>
			</server>
		</servers>
	</settings>
	
Then a release can be perfomed with

	mvn release:clean release:prepare
	
by answering the prompts for versions and tags, followed by
	
	mvn release:perform

Finally, put the release to the central repository by

	...
	cd target/checkout
	mvn nexus-staging:release -P release

Last but not least, do not forget to bump the versions in the examples in this file.
