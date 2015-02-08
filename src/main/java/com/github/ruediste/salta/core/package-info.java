/**
 * Core of the Framework.
 * 
 * <p>
 * The central concept of this package is the <b>lookup</b> of <b>dependencies</b> by a <b> key </b>, 
 * performed by the {@link com.github.ruediste.salta.core.CoreInjector Injector}.
 * The configuration of the lookup process is stored in an {@link com.github.ruediste.salta.core.CoreInjectorConfiguration InjectorConfiguration}.
 * Additional concepts are <b>circular dependency detection </b> and <b>scoping</b>.
 * </p>
 * 
 * <p>
 * The core package has been designed to be extremely focused, flexible and extensible. All non-core aspects have been delegated to strategies, which
 * need to be configured.
 * </p>
 * 
 * <p>
 * There are three places which are searched in order to lookup a dependency:
 * <ol>
 * <li> <b> DependencyFactories:</b> Create a dependency based on the key. Do not take part in circular dependency detection or scoping </li>
 * <li> <b> Static Bindings: </b> When the key matches the binding, the binding creates the dependency. It is important that no information from the key is 
 * used while creating the dependency. This enables circular dependency detection and scoping. A key can match multiple static bindings. In this case, an exception is thrown.</li>
 * <li> <b> JIT Bindings: </b> A {@link com.github.ruediste.salta.core.JITBindingKey JitBindingKey} is created from the key. 
 * Based on the JIT binding key, an existing {@link com.github.ruediste.salta.core.JITBinding JITBinding} is looked up. If no JIT binding is found,
 * the the {@link com.github.ruediste.salta.core.JITBindingRule JITBindingRules} are evaluated and an eventually created binding is used. JITBindings take part
 * in circular dependency detection and scoping as well.</li>    
 * </ol>
 * </p>
 * 
 */
package com.github.ruediste.salta.core;

