package io.github.evacchi;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import org.reflections.Reflections;

public class Binder {

    private final HashMap<Class<?>, Constructor<?>> bindings = new HashMap<>();

    public Binder scan() {
        Reflections reflections = new Reflections();
        reflections.getTypesAnnotatedWith(InjectCandidate.class)
                .forEach(t -> bindings.put(interfaceOf(t), constructorOf(t)));
        return this;
    }

    public <T> T createInstance(Class<? extends T> t) {
        return (T) Arrays.stream(t.getDeclaredConstructors())
                .filter(c -> c.getAnnotation(Inject.class) != null)
                .peek(c -> c.setAccessible(true))
                .map(this::createInstance)
                .findFirst().get();

    }

    private Class<?> interfaceOf(Class<?> t) {
        return t.getInterfaces()[0];
    }

    private <T> T createInstance(Constructor<? extends T> cons) {
        Object[] r =
                Arrays.stream(cons.getParameterTypes())
                        .map(bindings::get)
                        .filter(Objects::nonNull)
                        .map(this::createInstance)
                        .toArray();
        try {
            return cons.newInstance(r);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private <T> Constructor<? extends T> constructorOf(Class<? extends T> t) {
        try {
            return t.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
}
