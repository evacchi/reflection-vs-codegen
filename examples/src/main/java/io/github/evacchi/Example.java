package io.github.evacchi;

import javax.inject.Inject;

public class Example {
    private final Animal animal;

    @Inject
    public Example(Animal animal) {
        this.animal = animal;
    }

    public Animal animal() {
        return animal;
    }
}