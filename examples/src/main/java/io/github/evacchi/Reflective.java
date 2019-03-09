package io.github.evacchi;

import java.util.Objects;

public class Reflective {

    public static void main(String[] args) {
        Binder binder = new Binder();
        binder.scan();
        Example ex = binder.createInstance(Example.class);
        Animal animal = ex.animal();
        Objects.requireNonNull(animal);
        System.out.println(animal);
    }
}
