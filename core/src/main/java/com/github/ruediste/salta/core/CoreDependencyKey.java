package com.github.ruediste.salta.core;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Objects;

import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.reflect.TypeToken;

/**
 * Key to lookup a dependency.
 */
public abstract class CoreDependencyKey<T> {

    /**
     * Get the required type the looked up dependency
     */
    public abstract TypeToken<T> getType();

    public abstract Class<T> getRawType();

    /**
     * Get the {@link AnnotatedElement} representing the annotations affecting
     * the lookup. Usually, this is just the {@link Field} or {@link Parameter}
     * which is beeing injected. If dependencies are looked up directly from the
     * injector, this can also be a synthetic implementation.
     */
    public abstract AnnotatedElement getAnnotatedElement();

    @Override
    public String toString() {
        return getType().toString();
    }

    public static Matcher<CoreDependencyKey<?>> typeMatcher(TypeToken<?> type) {
        return new TypeTokenMatcher(type);
    }

    private static final class EqualityMatcher
            implements Matcher<CoreDependencyKey<?>> {
        private CoreDependencyKey<?> key;

        public EqualityMatcher(CoreDependencyKey<?> key) {
            this.key = key;
        }

        @Override
        public boolean matches(CoreDependencyKey<?> t) {
            return Objects.equals(key, t);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof EqualityMatcher) {
                return Objects.equals(key, ((EqualityMatcher) obj).key);
            }
            return false;
        }

        @Override
        public String toString() {
            return "=" + key;
        }
    }

    public static class TypeTokenMatcher
            implements Matcher<CoreDependencyKey<?>> {

        private TypeToken<?> type;

        public TypeTokenMatcher(TypeToken<?> type) {
            this.type = type;
        }

        @Override
        public boolean matches(CoreDependencyKey<?> key) {
            return key.getType().equals(type);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null)
                return false;
            if (obj.getClass() != getClass())
                return false;
            TypeTokenMatcher other = (TypeTokenMatcher) obj;
            return Objects.equals(type, other.type);

        }

        @Override
        public String toString() {
            return "type=" + type;
        }
    }

    public static Matcher<CoreDependencyKey<?>> rawTypeMatcher(Class<?> type) {
        return new RawTypeTokenMatcher(type);
    }

    public static class RawTypeTokenMatcher
            implements Matcher<CoreDependencyKey<?>> {

        private Class<?> type;

        public RawTypeTokenMatcher(Class<?> type) {
            this.type = type;
        }

        @Override
        public boolean matches(CoreDependencyKey<?> key) {
            return key.getRawType().equals(type);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null)
                return false;
            if (obj.getClass() != getClass())
                return false;
            RawTypeTokenMatcher other = (RawTypeTokenMatcher) obj;
            return Objects.equals(type, other.type);
        }

        @Override
        public String toString() {
            return "rawType=" + type;
        }
    }

    public static Matcher<CoreDependencyKey<?>> matcher(
            CoreDependencyKey<?> key) {
        return new EqualityMatcher(key);

    }
}
