# Salta Dependency Injection Framework

 * Familiar: same configuration API as Guice, similar concepts
 * Fast: fast startup, fast object instantiation
 * Flexible: customize API, annotations, behaviour ...
 
** PASSES JSR330 (javax.inject) Technology Compatibility Kit (TCK) **

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

[Documentation](wiki/) 

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

The wiki contains more information about extending and customizing Salta.

## Motivation

While developing a full stack web framework, we were looking for a dependency injection framework allowing for a quick application restart, even for large applications. 

This ruled out (JavaEE CDI)[http://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html] since the set of available beans (the counterpart of bindings in Guice) is determined when the container is initialized and cannot be changed afterwards. This implies that the available classes are scanned during startup, which results in slow startup for large applications.

In (Guice)[https://github.com/google/guice], the bindings can be created while the container is running (JIT bindings). However there is no way to add a standard dependency (annotated with @Inject) to an injection point specific instance. (As it would for example be necessary to nicely integrate slf4j loggers). Also, the JIT binding rules are fixed and the framework already brings two sets of annotations to the classpath (com.google.inject.* and javax.inject.*)

Spring is known to be very slow.

For these reasons, we started the Salta project.

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
