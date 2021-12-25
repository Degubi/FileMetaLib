package mediaprops;

import java.util.*;

public final class MediaPropertyMap {
    final HashMap<MediaProperty<?>, Object> storage = new HashMap<>();

    private MediaPropertyMap() {}

    public static MediaPropertyMap empty() { return new MediaPropertyMap(); };

    public<T> T get(MediaProperty<T> property) {
        var result = (T) storage.get(property);

        if(result == null) {
            throw new IllegalArgumentException("Property '" + property.name + " is missing");
        }

        return result;
    }

    public<T> T getOrDefault(MediaProperty<T> property, T defaultValue) {
        var result = (T) storage.get(property);

        return result == null ? defaultValue : result;
    }

    public<T> Optional<T> getOptional(MediaProperty<T> property) {
        return Optional.ofNullable((T) storage.get(property));
    }

    public boolean containsProperty(MediaProperty<?> property) {
        return storage.containsKey(property);
    }

    public<T> void put(MediaProperty<T> property, T value) {
        storage.put(property, value);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaPropertyMap) {
            return ((MediaPropertyMap) obj).storage.equals(storage);
        }

        return false;
    }

    @Override
    public int hashCode() { return storage.hashCode(); }

    @Override
    public String toString() { return storage.toString(); }
}