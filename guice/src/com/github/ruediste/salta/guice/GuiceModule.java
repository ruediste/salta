package com.github.ruediste.salta.guice;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.CreationRuleImpl;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.standard.DefaultFixedConstructorInstantiationRule;
import com.github.ruediste.salta.standard.DefaultJITBindingKeyRule;
import com.github.ruediste.salta.standard.DefaultJITBindingRule;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.ProviderMethodBinder;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.DefaultConstructionRule;
import com.github.ruediste.salta.standard.config.MembersInjectorFactory;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.github.ruediste.salta.standard.util.ImplementedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.MembersInjectorCreationRuleBase;
import com.github.ruediste.salta.standard.util.MembersInjectorFactoryBase;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.github.ruediste.salta.standard.util.ProvidedByConstructionRuleBase;
import com.github.ruediste.salta.standard.util.ProviderCreationRule;
import com.github.ruediste.salta.standard.util.StaticMembersInjectorBase;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.BindingAnnotation;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

public class GuiceModule implements Module {

    private GuiceInjectorConfiguration guiceConfig;
    private GuiceInjectorImpl injector;
    private StandardInjectorConfiguration config;
    private Binder binder;

    public GuiceModule(GuiceInjectorConfiguration guiceConfig,
            GuiceInjectorImpl injector) {
        this.guiceConfig = guiceConfig;
        this.injector = injector;
    }

    protected Binder binder() {
        return binder;
    }

    @Override
    public void configure(Binder binder) {

        this.binder = binder;
        config = binder().getGuiceConfiguration().config;
        addLoggerCreationRule();

        addQualifierMatchingRules();

        addStageCreationRule();

        addMembersInjectorFactory();

        addProviderCrationRules();

        addMembersInjectorCreationRule();

        addQualifierExtractors();

        addTypeLiteralCreationRule();

        binder().getDelegate().bind(Injector.class).toInstance(injector);

        addStaticMembersInjectionDynamicInitializer();

        addProviderMethodBinderModulePostProcessor();

        addCheckModulePostProcessor();

        addProvidedByConstructionRule();

        if (guiceConfig.requireExplicitBindings) {

            for (TypeToken<?> type : guiceConfig.typesBoundToDefaultCreationRecipe) {
                ProvidedBy providedBy = type.getRawType()
                        .getAnnotation(ProvidedBy.class);
                if (providedBy != null) {
                    StandardStaticBinding binding = new StandardStaticBinding();
                    binding.dependencyMatcher = DependencyKey
                            .rawTypeMatcher(providedBy.value());
                    binding.recipeFactory = ctx -> config.construction
                            .createConcreteConstructionRecipe(
                                    TypeToken.of(providedBy.value()), ctx);
                    binding.scopeSupplier = () -> config.scope
                            .getScope(providedBy.value());
                    guiceConfig.automaticStaticBindings.add(binding);
                }
            }

            HashSet<Class<?>> rawTypes = new HashSet<Class<?>>();
            for (TypeToken<?> type : guiceConfig.typesBoundToDefaultCreationRecipe) {
                ImplementedBy implementedBy = type.getRawType()
                        .getAnnotation(ImplementedBy.class);
                if (implementedBy != null) {
                    if (rawTypes.add(implementedBy.value())) {
                        StandardStaticBinding binding = new StandardStaticBinding();
                        binding.dependencyMatcher = DependencyKey
                                .rawTypeMatcher(implementedBy.value());
                        binding.recipeFactory = ctx -> config.construction
                                .createConcreteConstructionRecipe(
                                        TypeToken.of(implementedBy.value()),
                                        ctx);
                        binding.scopeSupplier = () -> config.scope
                                .getScope(implementedBy.value());
                        guiceConfig.automaticStaticBindings.add(binding);
                    }
                }
            }

            HashSet<CoreDependencyKey<?>> keys = new HashSet<>();
            for (CoreDependencyKey<?> foo : guiceConfig.implicitlyBoundKeys) {
                if (keys.add(foo)) {
                    StandardStaticBinding binding = new StandardStaticBinding();
                    binding.dependencyMatcher = DependencyKey.matcher(foo);
                    binding.recipeFactory = ctx -> config.construction
                            .createConcreteConstructionRecipe(foo.getType(),
                                    ctx);
                    binding.scopeSupplier = () -> config.scope
                            .getScope(foo.getType());
                    guiceConfig.automaticStaticBindings.add(binding);
                }
            }
        } else {
            config.creationPipeline.jitBindingKeyRules
                    .add(new DefaultJITBindingKeyRule(config));

            config.creationPipeline.jitBindingRules
                    .add(new DefaultJITBindingRule(config));
        }

        addImplementedByConstructionRule();

        addConstructorInstantiationRule();

        config.fixedConstructorInstantiatorFactoryRules
                .add(new DefaultFixedConstructorInstantiationRule(config));
        config.injectionOptionalRules.add(e -> {
            Inject i = e.getAnnotation(Inject.class);
            if (i != null && i.optional() == true)
                return Optional.of(true);

            else
                return Optional.of(false);
        });

        if (guiceConfig.stage == Stage.PRODUCTION)
            addEagerInstantiationDynamicInitializer();

        binder().getDelegate().bindScope(Singleton.class,
                config.singletonScope);
        binder().getDelegate().bindScope(javax.inject.Singleton.class,
                config.singletonScope);

        config.construction.constructionRules
                .add(new DefaultConstructionRule(config));
        addStageCreationRule();
        config.membersInjectorFactory = new MembersInjectorFactory() {

            @Override
            public <T> Consumer<T> createMembersInjector(TypeToken<T> type) {
                MembersInjector<T> inner = injector
                        .getMembersInjector(TypeLiteral.get(type));
                return new Consumer<T>() {

                    @Override
                    public void accept(T t) {
                        inner.injectMembers(t);
                    }

                    @Override
                    public String toString() {
                        return inner.toString();
                    }
                };
            }
        };

        config.creationPipeline.coreCreationRuleSuppliers
                .add(config.creationPipeline.coreCreationRuleSuppliers.indexOf(
                        config.creationPipeline.suppliers.staticBindingsSupplier)
                        + 1, guiceConfig.automaticStaticBindingsSupplier);
    }

    private void addStageCreationRule() {
        // stage rule
        config.creationPipeline.creationRules.add(new CreationRule() {

            @Override
            public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
                    CoreDependencyKey<?> key, CoreInjector injector) {
                if (Stage.class.equals(key.getType().getType()))
                    return Optional.of(ctx -> new SupplierRecipeImpl(
                            () -> guiceConfig.stage));
                else
                    return Optional.empty();
            }

        });
    }

    private void addProvidedByConstructionRule() {
        // rule for @ProvidedBy
        config.construction.constructionRules
                .add(new ProvidedByConstructionRuleBase(Provider.class) {

                    @Override
                    protected DependencyKey<?> getProviderKey(
                            TypeToken<?> type) {
                        ProvidedBy providedBy = type.getRawType()
                                .getAnnotation(ProvidedBy.class);
                        if (providedBy != null) {
                            if (!type.isAssignableFrom(
                                    TypeToken.of(providedBy.value())
                                            .resolveType(Provider.class
                                                    .getTypeParameters()[0]))) {
                                throw new SaltaException(
                                        "Provider " + providedBy.value()
                                                + " specified by @ProvidedBy does not provide "
                                                + type);
                            }
                            return DependencyKey.of(providedBy.value());
                        }
                        return null;
                    }
                });
    }

    private void addCheckModulePostProcessor() {
        guiceConfig.modulePostProcessors.add(new Consumer<Module>() {
            @Override
            public void accept(Module module) {
                Class<?> cls = module.getClass();
                MethodOverrideIndex idx = new MethodOverrideIndex(cls);
                while (!Object.class.equals(cls)) {
                    for (Method m : cls.getDeclaredMethods()) {
                        if (!idx.wasScanned(m))
                            continue;
                        Provides provides = m.getAnnotation(Provides.class);
                        if (provides == null)
                            continue;
                        if (idx.isOverridden(m)) {
                            throw new SaltaException(
                                    "Overriding @Provides methods is not allowed:\n "
                                            + m + "\nis overridden by \n "
                                            + idx.getOverridingMethods(m)
                                                    .stream()
                                                    .map(java.util.Objects::toString)
                                                    .collect(joining("\n ")));
                        }
                    }
                    cls = cls.getSuperclass();
                }
            }
        });
    }

    private void addProviderMethodBinderModulePostProcessor() {
        ProviderMethodBinder b = new ProviderMethodBinder(config) {

            @Override
            protected boolean isProviderMethod(Method m) {
                if (!m.isAnnotationPresent(Provides.class))
                    return false;
                if (void.class.equals(m.getReturnType())) {
                    throw new SaltaException(
                            "@Provides method may not return void:\n" + m);
                }

                return true;
            }
        };
        guiceConfig.modulePostProcessors.add(m -> b.bindProviderMethodsOf(m));
    }

    private void addStaticMembersInjectionDynamicInitializer() {
        // register initializer for requested static injections
        config.dynamicInitializers.add(() -> new StaticMembersInjectorBase() {

            @Override
            protected InjectionInstruction shouldInject(Method method) {
                Inject guiceInject = method.getAnnotation(Inject.class);
                javax.inject.Inject inject = method
                        .getAnnotation(javax.inject.Inject.class);
                if (guiceInject == null && inject == null) {
                    return InjectionInstruction.NO_INJECT;
                }
                return config.isInjectionOptional(method)
                        ? InjectionInstruction.INJECT_OPTIONAL
                        : InjectionInstruction.INJECT;
            }

            @Override
            protected InjectionInstruction shouldInject(Field field) {
                Inject guiceInject = field.getAnnotation(Inject.class);
                javax.inject.Inject inject = field
                        .getAnnotation(javax.inject.Inject.class);
                if (guiceInject == null && inject == null) {
                    return InjectionInstruction.NO_INJECT;
                }
                return config.isInjectionOptional(field)
                        ? InjectionInstruction.INJECT_OPTIONAL
                        : InjectionInstruction.INJECT;
            }
        }.injectStaticMembers(config, injector.getSaltaInjector()));
    }

    private void addEagerInstantiationDynamicInitializer() {
        config.dynamicInitializers.add(() -> {
            injector.getSaltaInjector().getCoreInjector()
                    .withRecipeCreationContext(ctx -> {
                for (Binding b : config.creationPipeline.staticBindings) {
                    b.getScope().performEagerInstantiation(ctx, b);
                }
                return null;
            });
        });
    }

    private void addConstructorInstantiationRule() {
        // rule for constructor instantiation
        config.construction.instantiatorRules
                .add(new ConstructorInstantiatorRuleBase(config) {

                    @Override
                    public Optional<Function<RecipeCreationContext, RecipeInstantiator>> apply(
                            TypeToken<?> typeToken) {

                        if (TypeLiteral.class.equals(typeToken.getType())) {
                            throw new SaltaException(
                                    "Cannot inject a TypeLiteral that has no type parameter");
                        }

                        return super.apply(typeToken);
                    }

                    @Override
                    protected Integer getConstructorPriority(Constructor<?> c) {
                        Inject guiceInject = c.getAnnotation(Inject.class);
                        if (guiceInject != null && guiceInject.optional()) {
                            throw new SaltaException(c
                                    + " is annotated @Inject(optional=true), but constructors cannot be optional");
                        }
                        if (c.isAnnotationPresent(Inject.class) || c
                                .isAnnotationPresent(javax.inject.Inject.class))
                            return 2;
                        boolean isInnerClass = c.getDeclaringClass()
                                .getEnclosingClass() != null;

                        if (guiceConfig.requireAtInjectOnConstructors)
                            return null;

                        if (c.getParameterCount() == 0
                                && (Modifier.isPublic(c.getModifiers())
                                        || isInnerClass))
                            return 1;
                        return null;
                    }
                });
    }

    private void addImplementedByConstructionRule() {
        // rule for @ImplementedBy
        config.construction.constructionRules
                .add(new ImplementedByConstructionRuleBase() {

                    @Override
                    protected DependencyKey<?> getImplementorKey(
                            TypeToken<?> type) {
                        ImplementedBy implementedBy = type.getRawType()
                                .getAnnotation(ImplementedBy.class);
                        if (implementedBy != null) {

                            if (type.getRawType()
                                    .equals(implementedBy.value())) {
                                throw new SaltaException(
                                        "@ImplementedBy points to the same class it annotates. type: "
                                                + type);
                            }
                            return DependencyKey.of(implementedBy.value());
                        }
                        return null;
                    }
                });
    }

    private void addTypeLiteralCreationRule() {
        // Rule for type literals
        config.creationPipeline.creationRules.add(new CreationRuleImpl(
                k -> TypeLiteral.class.equals(k.getRawType()), key -> {
                    TypeToken<?> type = key.getType();
                    if (type.getType() instanceof Class) {
                        throw new SaltaException(
                                "Cannot inject a TypeLiteral that has no type parameter");
                    }
                    TypeToken<?> typeParameter = type.resolveType(
                            TypeLiteral.class.getTypeParameters()[0]);
                    if (typeParameter.getType() instanceof TypeVariable)
                        throw new SaltaException("TypeLiteral<" + typeParameter
                                + "> cannot be used as a key; It is not fully specified.");
                    TypeLiteral<?> typeLiteral = TypeLiteral
                            .get(typeParameter.getType());
                    return () -> typeLiteral;
                }));
    }

    private void addQualifierExtractors() {
        // qualifiers
        config.requiredQualifierExtractors.add(annotatedElement -> {
            return Arrays.stream(annotatedElement.getAnnotations())
                    .filter(a -> a.annotationType()
                            .isAnnotationPresent(BindingAnnotation.class)
                            || a.annotationType().isAnnotationPresent(
                                    javax.inject.Qualifier.class));
        });

        config.availableQualifierExtractors
                .add(new Function<AnnotatedElement, Stream<Annotation>>() {
                    @Override
                    public Stream<Annotation> apply(
                            AnnotatedElement annotated) {
                        return Arrays.stream(annotated.getAnnotations())
                                .filter(a -> a.annotationType()
                                        .isAnnotationPresent(
                                                BindingAnnotation.class)
                                        || a.annotationType()
                                                .isAnnotationPresent(
                                                        javax.inject.Qualifier.class));
                    }
                });
    }

    private void addMembersInjectorCreationRule() {
        // members injector creation rule
        config.creationPipeline.creationRules
                .add(new MembersInjectorCreationRuleBase(config) {
                    @Override
                    protected TypeToken<?> getDependency(
                            CoreDependencyKey<?> key) {
                        Class<?> rawType = key.getRawType();
                        if (MembersInjector.class.equals(key.getRawType())) {
                            if (key.getType().getType() instanceof Class) {
                                throw new SaltaException(
                                        "Cannot inject a MembersInjector that has no type parameter");
                            }
                            TypeToken<?> dependency = key.getType().resolveType(
                                    rawType.getTypeParameters()[0]);
                            return dependency;
                        } else
                            return null;
                    }

                    @Override
                    protected Object wrapInjector(
                            Consumer<Object> saltaMembersInjector) {
                        return new MembersInjector<Object>() {
                            @Override
                            public void injectMembers(Object x) {
                                saltaMembersInjector.accept(x);
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
                });

    }

    private void addProviderCrationRules() {
        // provider creation rule
        config.creationPipeline.creationRules
                .add(new ProviderCreationRule(
                        key -> key.getRawType().equals(
                                Provider.class),
                (type, supplier) -> (Provider<Object>) () -> supplier.get(),
                Provider.class));
        config.creationPipeline.creationRules
                .add(new ProviderCreationRule(
                        key -> key.getRawType()
                                .equals(javax.inject.Provider.class),
                        (type, supplier) -> (javax.inject.Provider<Object>) () -> supplier
                                .get(),
                        javax.inject.Provider.class));
    }

    private void addMembersInjectorFactory() {
        config.construction.membersInjectorFactories
                .add(new MembersInjectorFactoryBase(config) {

                    @Override
                    protected InjectionInstruction getInjectionInstruction(
                            TypeToken<?> declaringType, Method method,
                            MethodOverrideIndex index) {
                        Inject guiceInject = method.getAnnotation(Inject.class);
                        javax.inject.Inject inject = method
                                .getAnnotation(javax.inject.Inject.class);
                        if (guiceInject == null && inject == null)
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

                        return config.isInjectionOptional(method)
                                ? InjectionInstruction.INJECT_OPTIONAL
                                : InjectionInstruction.INJECT;
                    }

                    @Override
                    protected InjectionInstruction getInjectionInstruction(
                            TypeToken<?> declaringType, Field field) {
                        Inject guiceInject = field.getAnnotation(Inject.class);
                        javax.inject.Inject inject = field
                                .getAnnotation(javax.inject.Inject.class);
                        if (guiceInject == null && inject == null)
                            if (guiceInject == null && inject == null)
                                return InjectionInstruction.NO_INJECTION;

                        if (Modifier.isFinal(field.getModifiers())) {
                            throw new SaltaException(
                                    "Final field annotated with @Inject");
                        }
                        if (Modifier.isStatic(field.getModifiers()))
                            return InjectionInstruction.NO_INJECTION;

                        return config.isInjectionOptional(field)
                                ? InjectionInstruction.INJECT_OPTIONAL
                                : InjectionInstruction.INJECT;
                    }
                });
    }

    private void addQualifierMatchingRules() {
        // make Named annotations of javax.inject and Guice equivalent
        config.qualifierMatchingAnnotationRules
                .add(new BiFunction<Annotation, Annotation, Boolean>() {
                    @Override
                    public Boolean apply(Annotation required,
                            Annotation available) {
                        if (required instanceof javax.inject.Named
                                && available instanceof Named) {
                            return Objects.equal(
                                    ((javax.inject.Named) required).value(),
                                    ((Named) available).value());
                        }
                        if (available instanceof javax.inject.Named
                                && required instanceof Named) {
                            return Objects.equal(((Named) required).value(),
                                    ((javax.inject.Named) available).value());
                        }
                        return null;
                    }
                });

        config.qualifierMatchingTypeRules
                .add(new BiFunction<Annotation, Class<?>, Boolean>() {

                    @Override
                    public Boolean apply(Annotation required,
                            Class<?> availableType) {
                        if (Named.class.equals(availableType)
                                || javax.inject.Named.class
                                        .equals(availableType)) {
                            return Named.class.equals(required.annotationType())
                                    || javax.inject.Named.class
                                            .equals(required.annotationType());
                        }
                        return null;
                    }
                });
    }

    private void addLoggerCreationRule() {
        // rule for loggers
        config.creationPipeline.creationRules.add(new CreationRule() {

            @Override
            public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
                    CoreDependencyKey<?> key, CoreInjector injector) {

                Class<?> rawType = key.getRawType();
                if (Logger.class.equals(rawType)) {
                    return Optional.of(ctx -> {
                        if (key instanceof InjectionPoint) {
                            Member member = ((InjectionPoint<?>) key)
                                    .getMember();
                            return new SupplierRecipeImpl(
                                    () -> Logger.getLogger(member
                                            .getDeclaringClass().getName()));

                        }
                        return new SupplierRecipeImpl(
                                () -> Logger.getAnonymousLogger());
                    });
                }
                return Optional.empty();
            }
        });
    }
}
