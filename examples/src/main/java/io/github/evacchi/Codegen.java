package io.github.evacchi;

import java.util.Objects;

public class Codegen {

    public static void main(String[] args) {
        GeneratedBinder binder = new GeneratedBinder();
        Example ex = binder.createInstance(Example.class);
        Animal animal = ex.animal();
        Objects.requireNonNull(animal);
        System.out.println(animal);
    }
}
