package net.xolt.sbutils.feature;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class Features {

    private final Map<Class<? extends Feature>, Feature> features = new IdentityHashMap<>();

    @SafeVarargs public final <T extends Feature> void add(T... features) {
        for (Feature feature : features) {
            this.features.put(feature.getClass(), feature);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Feature> T get(Class<T> feature) {
        return (T) features.get(feature);
    }

    public Collection<Feature> getAll() {
        return features.values();
    }
}
