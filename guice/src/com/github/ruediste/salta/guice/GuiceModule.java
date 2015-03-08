package com.github.ruediste.salta.guice;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.jsr330.JSR330ConstructorInstantiatorRule;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.ProviderMethodBinder;
import com.github.ruediste.salta.standard.config.InstantiatorRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.util.ImplementedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.ProvidedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.ProviderDependencyFactoryRule;
import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

public class GuiceModule extends AbstractModule {

	private GuiceInjectorConfiguration guiceConfig;
	private GuiceInjectorImpl injector;

	public GuiceModule(GuiceInjectorConfiguration guiceConfig,
			GuiceInjectorImpl injector) {
		this.guiceConfig = guiceConfig;
		this.injector = injector;
	}

	@Override
	protected void configure() {

		StandardInjectorConfiguration config = binder().getConfiguration();

		config.config.creationRules.add(new CreationRule() {

			@Override
			public SupplierRecipe apply(CoreDependencyKey<?> key,
					RecipeCreationContext ctx) {
				if (Stage.class.equals(key.getType().getType()))
					return new SupplierRecipeImpl(() -> guiceConfig.stage);
				else
					return null;
			}
		});

		config.defaultMembersInjectorFactories
				.add(new GuiceMembersInjectorFactory(config.config));

		// provider creation rule
		config.config.creationRules
				.add(new ProviderDependencyFactoryRule(key -> key.getRawType()
						.equals(Provider.class),
						(type, supplier) -> (Provider<?>) supplier::get,
						Provider.class));

		// members injector creation rule
		config.config.creationRules.add(new CreationRule() {

			@Override
			public SupplierRecipe apply(CoreDependencyKey<?> key,
					RecipeCreationContext ctx) {
				if (MembersInjector.class.equals(key.getRawType())) {
					TypeToken<?> dependency = key.getType().resolveType(
							MembersInjector.class.getTypeParameters()[0]);
					injector.getSaltaInjector().getMembersInjector(dependency);
					foo
				}
				return null;
			}
		});

		config.requiredQualifierExtractors.add(key -> {
			return Arrays.stream(key.getAnnotatedElement().getAnnotations())
					.filter(a -> a.annotationType().isAnnotationPresent(
							BindingAnnotation.class));
		});

		config.availableQualifierExtractors.add(annotated -> Arrays.stream(
				annotated.getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(
						BindingAnnotation.class)));

		// Rule for type literals
		config.config.creationRules
				.add(new CreationRuleImpl(
						k -> TypeLiteral.class.equals(k.getRawType()),
						key -> {
							TypeToken<?> type = key.getType();
							if (type.getType() instanceof Class) {
								throw new SaltaException(
										"Cannot inject a TypeLiteral that has no type parameter");
							}
							TypeToken<?> typeParameter = type
									.resolveType(TypeLiteral.class
											.getTypeParameters()[0]);
							if (typeParameter.getType() instanceof TypeVariable)
								throw new SaltaException(
										"TypeLiteral<"
												+ typeParameter
												+ "> cannot be used as a key; It is not fully specified.");
							TypeLiteral<?> typeLiteral = TypeLiteral
									.get(typeParameter.getType());
							return () -> typeLiteral;
						}));

		bind(Injector.class).toInstance(injector);

		// register initializer for requested static injections
		config.dynamicInitializers.add(i -> new GuiceStaticMemberInjector()
				.injectStaticMembers(config, i));

		// scan modules for @Provides methods
		{
			ProviderMethodBinder b = new ProviderMethodBinder(config) {

				@Override
				protected boolean isProviderMethod(Method m) {
					if (!m.isAnnotationPresent(Provides.class))
						return false;
					if (void.class.equals(m.getReturnType())) {
						throw new SaltaException(
								"@Provides method may not return void");
					}
					return true;
				}
			};
			config.modulePostProcessors.add(b::bindProviderMethodsOf);
		}
		bindScope(Singleton.class, config.singletonScope);

		config.staticInitializers.add(injector::setDelegate);

		// add rule for ProvidedBy and ImplementedBy
		config.constructionRules.add(new ProvidedByConstructionRuleBase(
				Provider.class) {

			@Override
			protected DependencyKey<?> getProviderKey(TypeToken<?> type) {
				ProvidedBy providedBy = type.getRawType().getAnnotation(
						ProvidedBy.class);
				if (providedBy != null)
					return DependencyKey.of(providedBy.value());
				return null;
			}
		});

		config.constructionRules.add(new ImplementedByConstructionRuleBase() {

			@Override
			protected DependencyKey<?> getImplementorKey(TypeToken<?> type) {
				ImplementedBy implementedBy = type.getRawType().getAnnotation(
						ImplementedBy.class);
				if (implementedBy != null)
					return DependencyKey.of(implementedBy.value());
				return null;
			}
		});
		{
			install(new JSR330Module());
			List<InstantiatorRule> tmp = new ArrayList<>(
					config.instantiatorRules);
			config.instantiatorRules.clear();
			config.instantiatorRules
					.addAll(tmp
							.stream()
							.filter(x -> !(x instanceof JSR330ConstructorInstantiatorRule))
							.collect(toList()));
		}

		config.instantiatorRules.add(new GuiceConstructorInstantiatorRule(
				config.config, guiceConfig.requireAtInjectOnConstructors));

		if (guiceConfig.requireExplicitBindings)
			config.config.jitBindingRules.clear();
	}
}
