package com.github.ruediste.salta.guice.binder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.core.StaticBindingSet;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;
import com.google.inject.Module;
import com.google.inject.Stage;

public class GuiceInjectorConfiguration {

    public GuiceInjectorConfiguration(Stage stage) {
        this.stage = stage;
        config = new StandardInjectorConfiguration(stage.getSaltaStage());
    }

    public final StandardInjectorConfiguration config;

    public final Stage stage;
    public boolean requireExplicitBindings;
    public boolean requireAtInjectOnConstructors;
    public boolean requireExactBindingAnnotations;

    /**
     * List of modules that were used to create this configuration. Used to post
     * process the modules after the first configuration
     */
    public List<Module> modules = new ArrayList<>();

    public List<Consumer<Module>> modulePostProcessors = new ArrayList<>();

    public void postProcessModules() {
        for (Module module : modules) {
            modulePostProcessors.stream().forEach(x -> x.accept(module));
        }
    }

    public final ArrayList<TypeToken<?>> typesBoundToDefaultCreationRecipe = new ArrayList<>();

    public final ArrayList<CoreDependencyKey<?>> implicitlyBoundKeys = new ArrayList<>();

    /**
     * Automatically defined statically defined bindings. These bindings are
     * evaluated after the
     * {@link StandardInjectorConfiguration.CreationPipelineConfiguration.CreationPipelineSuppliers#staticBindingsSupplier}
     * , but before the
     * {@link StandardInjectorConfiguration.CreationPipelineConfiguration.CreationPipelineSuppliers#jitRulesSupplier}
     */
    public final List<StaticBinding> automaticStaticBindings = new ArrayList<>();

    public final Supplier<CreationRule> automaticStaticBindingsSupplier = () -> new StaticBindingSet(
            automaticStaticBindings);
}
