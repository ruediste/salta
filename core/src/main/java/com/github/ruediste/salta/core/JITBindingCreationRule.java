package com.github.ruediste.salta.core;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class JITBindingCreationRule implements CreationRule {

    private List<JITBindingKeyRule> keyRules;
    private List<JITBindingRule> bindingRules;
    private HashMap<JITBindingKey, JITBinding> jitBindings = new HashMap<>();

    public JITBindingCreationRule(List<JITBindingKeyRule> keyRules,
            List<JITBindingRule> bindingRules) {
        this.keyRules = keyRules;
        this.bindingRules = bindingRules;

    }

    @Override
    public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
            CoreDependencyKey<?> key, CoreInjector injector) {
        // create key
        JITBindingKey jitKey = new JITBindingKey();
        for (JITBindingKeyRule rule : keyRules) {
            rule.apply(key, jitKey);
        }

        // check existing bindings and create new one if necessary
        JITBinding jitBinding;
        jitBinding = jitBindings.get(jitKey);
        if (jitBinding == null) {
            for (JITBindingRule rule : bindingRules) {
                jitBinding = rule.apply(jitKey);
                if (jitBinding != null) {
                    jitBindings.put(jitKey, jitBinding);
                    break;
                }
            }
        }

        // use binding if available
        if (jitBinding != null) {
            JITBinding tmp = jitBinding;
            return Optional
                    .of(ctx -> tmp.getScope().createRecipe(ctx, tmp, key));
        }
        return Optional.empty();
    }

}
