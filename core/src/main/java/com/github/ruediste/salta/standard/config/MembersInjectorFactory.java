package com.github.ruediste.salta.standard.config;

import java.util.function.Consumer;

import com.google.common.reflect.TypeToken;

public interface MembersInjectorFactory {

    <T> Consumer<T> createMembersInjector(TypeToken<T> type);
}
