package com.example.fitnessBackEnd;

public class StaticObjectProvider<T> {
    private final T instance;

    public StaticObjectProvider(T instance) {
        this.instance = instance;
    }

    public T getObject() {
        return instance;
    }
}
