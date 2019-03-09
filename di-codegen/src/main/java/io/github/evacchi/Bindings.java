package io.github.evacchi;

import java.util.Collection;
import java.util.HashMap;

import com.github.javaparser.ast.expr.ObjectCreationExpr;

public class Bindings {

    private final HashMap<String, ObjectCreationExpr> map = new HashMap<>();

    public Bindings() {
    }

    public void put(String interfaceCanonicalName, ObjectCreationExpr objectCreationExpr) {
        map.put(interfaceCanonicalName, objectCreationExpr);
    }

    /**
     * @param interfaceCanonicalName
     * @return implementation
     */
    public ObjectCreationExpr get(String interfaceCanonicalName) {
        return map.get(interfaceCanonicalName);
    }

    public Collection<String> interfaces() {
        return map.keySet();
    }

    @Override
    public String toString() {
        return "Bindings{" +
                map +
                '}';
    }
}
