package com.github.ruediste.salta.guice.binder;

import com.google.inject.binder.ConstantBindingBuilder;

public class ConstantBindingBuilderImpl implements ConstantBindingBuilder {

    private com.github.ruediste.salta.standard.binder.StandardConstantBindingBuilder delegate;

    public ConstantBindingBuilderImpl(
            com.github.ruediste.salta.standard.binder.StandardConstantBindingBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void to(String value) {
        delegate.to(value);
    }

    @Override
    public void to(int value) {
        delegate.to(value);
    }

    @Override
    public void to(long value) {
        delegate.to(value);

    }

    @Override
    public void to(boolean value) {
        delegate.to(value);
    }

    @Override
    public void to(double value) {
        delegate.to(value);

    }

    @Override
    public void to(float value) {
        delegate.to(value);

    }

    @Override
    public void to(short value) {
        delegate.to(value);

    }

    @Override
    public void to(char value) {
        delegate.to(value);

    }

    @Override
    public void to(byte value) {
        delegate.to(value);

    }

    @Override
    public void to(Class<?> value) {
        delegate.to(value);

    }

    @Override
    public <E extends Enum<E>> void to(E value) {
        delegate.to(value);

    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
