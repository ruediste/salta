package com.github.ruediste.salta.jsr330;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.DefaultJITBindingKeyRule;
import com.github.ruediste.salta.standard.DefaultJITBindingRule;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.MembersInjector;
import com.github.ruediste.salta.standard.ProviderMethodBinder;
import com.github.ruediste.salta.standard.Stage;
import com.github.ruediste.salta.standard.config.DefaultConstructionRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedConstructorRecipeInstantiator;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.github.ruediste.salta.standard.util.ImplementedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.MembersInjectorCreationRuleBase;
import com.github.ruediste.salta.standard.util.MembersInjectorFactoryBase;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.github.ruediste.salta.standard.util.ProvidedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.ProviderCreationRule;
import com.github.ruediste.salta.standard.util.RecipeInitializerFactoryBase;
import com.github.ruediste.salta.standard.util.StaticMembersInjectorBase;
import com.google.common.reflect.TypeToken;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		StandardInjectorConfiguration config = binder().getConfiguration();

		addProvidedByConstructionRule(config);
		addImplementedByConstructionRule(config);

		addConstructionInstantiatorRule(config);

		config.fixedConstructorInstantiatorFactory = (type, ctx, cstr) -> FixedConstructorRecipeInstantiator
				.of(type, ctx, cstr, config.config.injectionStrategy);

		// stage creation rule
		config.config.creationRules.add(new CreationRuleImpl(key -> Stage.class
				.equals(key.getRawType()), key -> () -> config.stage));

		addMembersInjectorFactory(config);

		addPostConstructInitializerFactory(config);

		addProviderCreationRule(config);

		addMembersInjectorCreationRule(config);
		addQualifierExtractors(config);

		addStaticMembersDynamicInitializer(config);

		addProviderMethodBinderModulePostProcessor(config);
		bindScope(Singleton.class, config.singletonScope);

		config.config.jitBindingKeyRules.add(new DefaultJITBindingKeyRule(
				config));

		config.config.jitBindingRules.add(new DefaultJITBindingRule(config));

		config.constructionRules.add(new DefaultConstructionRule(config));
	}

	private void addProviderMethodBinderModulePostProcessor(
			StandardInjectorConfiguration config) {
		// register scanner for provides methods
		{
			ProviderMethodBinder b = new ProviderMethodBinder(config) {

				@Override
				protected boolean isProviderMethod(Method m) {
					if (!m.isAnnotationPresent(Provides.class)) {
						return false;
					}
					if (void.class.equals(m.getReturnType())) {
						throw new SaltaException(
								"@Provides method returns void: " + m);
					}
					return true;
				}
			};
			config.modulePostProcessors.add(b::bindProviderMethodsOf);
		}
	}

	private void addStaticMembersDynamicInitializer(
			StandardInjectorConfiguration config) {
		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new StaticMembersInjectorBase() {
			@Override
			protected InjectionInstruction shouldInject(Method method) {
				return method.isAnnotationPresent(Inject.class) ? InjectionInstruction.INJECT
						: InjectionInstruction.NO_INJECT;
			}

			@Override
			protected InjectionInstruction shouldInject(Field field) {
				return field.isAnnotationPresent(Inject.class) ? InjectionInstruction.INJECT
						: InjectionInstruction.NO_INJECT;
			}
		}.injectStaticMembers(config, i));
	}

	private void addQualifierExtractors(StandardInjectorConfiguration config) {
		config.requiredQualifierExtractors.add(annotatedElement -> Arrays
				.stream(annotatedElement.getAnnotations()).filter(
						a -> a.annotationType().isAnnotationPresent(
								Qualifier.class)));

		config.availableQualifierExtractors.add(annotated -> Arrays.stream(
				annotated.getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(Qualifier.class)));
	}

	private void addMembersInjectorCreationRule(
			StandardInjectorConfiguration config) {
		// rule for members injectors
		config.config.creationRules.add(new MembersInjectorCreationRuleBase(
				config) {

			@Override
			protected Object wrapInjector(Consumer<Object> saltaMembersInjector) {
				return new MembersInjector<Object>() {

					@Override
					public void injectMembers(Object instance) {
						saltaMembersInjector.accept(instance);
					}

					@Override
					public String toString() {
						return saltaMembersInjector.toString();
					};
				};
			}

			@Override
			protected Class<?> getWrappedInjectorType() {
				return MembersInjector.class;
			}

			@Override
			protected TypeToken<?> getDependency(CoreDependencyKey<?> key) {
				if (!MembersInjector.class.equals(key.getRawType()))
					return null;

				if (key.getType().getType() instanceof Class) {
					throw new SaltaException(
							"Cannot inject a MembersInjector that has no type parameter");
				}
				TypeToken<?> dependency = key.getType().resolveType(
						MembersInjector.class.getTypeParameters()[0]);
				return dependency;
			}
		});
	}

	private void addProviderCreationRule(StandardInjectorConfiguration config) {
		config.config.creationRules.add(new ProviderCreationRule(key -> {
			return key.getType().getRawType().equals(Provider.class);
		}, (type, supplier) -> (Provider<?>) supplier::get, Provider.class));
	}

	private void addPostConstructInitializerFactory(
			StandardInjectorConfiguration config) {
		config.initializerFactories.add(new RecipeInitializerFactoryBase(
				config.config) {

			@Override
			protected boolean isInitializer(TypeToken<?> declaringType,
					Method method, MethodOverrideIndex overrideIndex) {

				if (method.isAnnotationPresent(PostConstruct.class)) {
					if (method.getTypeParameters().length > 0) {
						throw new SaltaException(
								"@PostConstruct methods may not declare generic type parameters");
					}
					return true;
				}

				return false;
			}
		});
	}

	private void addMembersInjectorFactory(StandardInjectorConfiguration config) {
		config.defaultMembersInjectorFactories
				.add(new MembersInjectorFactoryBase(config) {

					@Override
					protected InjectionInstruction getInjectionInstruction(
							TypeToken<?> declaringType, Method method,
							MethodOverrideIndex index) {
						if (!method.isAnnotationPresent(Inject.class))
							return InjectionInstruction.NO_INJECTION;
						if (Modifier.isAbstract(method.getModifiers()))
							throw new SaltaException(
									"Method annotated with @Inject is abstract: "
											+ method);
						if (method.getTypeParameters().length > 0) {
							throw new SaltaException(
									"Method is annotated with @Inject but declares type parameters. Method:\n"
											+ method);
						}
						if (index.isOverridden(method))
							return InjectionInstruction.NO_INJECTION;
						return InjectionInstruction.INJECT;
					}

					@Override
					protected InjectionInstruction getInjectionInstruction(
							TypeToken<?> declaringType, Field f) {
						boolean annotationPresent = f
								.isAnnotationPresent(Inject.class);
						if (annotationPresent
								&& Modifier.isFinal(f.getModifiers())) {
							throw new SaltaException(
									"Final field annotated with @Inject");
						}
						if (Modifier.isStatic(f.getModifiers()))
							return InjectionInstruction.NO_INJECTION;
						return annotationPresent ? InjectionInstruction.INJECT
								: InjectionInstruction.NO_INJECTION;
					}
				});
	}

	private void addConstructionInstantiatorRule(
			StandardInjectorConfiguration config) {
		// default instantiator rule
		config.instantiatorRules
				.add(new ConstructorInstantiatorRuleBase(config) {

					@Override
					protected Integer getConstructorPriority(Constructor<?> c) {
						if (c.isAnnotationPresent(Inject.class))
							return 2;
						boolean isInnerClass = c.getDeclaringClass()
								.getEnclosingClass() != null;

						if (c.getParameterCount() == 0
								&& (Modifier.isPublic(c.getModifiers()) || isInnerClass))
							return 1;
						return null;
					}

				});
	}

	private void addImplementedByConstructionRule(
			StandardInjectorConfiguration config) {
		config.constructionRules.add(new ImplementedByConstructionRuleBase() {
			@Override
			protected DependencyKey<?> getImplementorKey(TypeToken<?> type) {
				ImplementedBy implementedBy = type.getRawType().getAnnotation(
						ImplementedBy.class);

				if (implementedBy != null)
					return DependencyKey.of(implementedBy.value());
				else
					return null;
			}

		});
	}

	private void addProvidedByConstructionRule(
			StandardInjectorConfiguration config) {
		config.constructionRules.add(new ProvidedByConstructionRuleBase(
				Supplier.class) {
			@Override
			protected DependencyKey<?> getProviderKey(TypeToken<?> type) {
				ProvidedBy providedBy = type.getRawType().getAnnotation(
						ProvidedBy.class);

				if (providedBy != null)
					return DependencyKey.of(providedBy.value());
				else
					return null;
			}
		});
	}
}
