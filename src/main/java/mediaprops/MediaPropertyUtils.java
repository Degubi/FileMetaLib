package mediaprops;

import java.nio.file.*;

public final class MediaPropertyUtils {
    static {
        System.loadLibrary("MediaProps");
        init();
    }


    public static<T> T readProperty(Path file, MediaProperty<T> property) {
        var absPath = file.toAbsolutePath();
        var pathStr = absPath.toString();

        if(!Files.exists(absPath)) {
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }

        return switch(property.type) {
            case MediaProperty.STRING_FIELD_TYPE -> (T) readStringProperty(pathStr, property.propOrdinal);
            case MediaProperty.INT_FIELD_TYPE -> (T) (Integer) readIntProperty(pathStr, property.propOrdinal);
            default -> throw new IllegalStateException("I can't believe you've done this...");
        };
    }

    public static boolean hasProperty(Path file, MediaProperty<?> property) {
        var absPath = file.toAbsolutePath();

        if(!Files.exists(absPath)) {
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }

        return hasMediaProperty(absPath.toString(), property.propOrdinal);
    }

    public static<T> void writeProperty(Path file, MediaProperty<T> property, T value) {
        var absPath = file.toAbsolutePath();

        if(!Files.exists(absPath)) {
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }

        switch(property.type) {
            case MediaProperty.STRING_FIELD_TYPE -> writeStringProperty(absPath.toString(), property.propOrdinal, (String) value);
            case MediaProperty.INT_FIELD_TYPE    -> writeIntProperty(absPath.toString(), property.propOrdinal, (int) value);
            default -> throw new IllegalStateException("I can't believe you've done this...");
        }
    }

    private static native String readStringProperty(String filePath, int propertyKey);
    private static native int readIntProperty(String filePath, int propertyKey);

    private static native boolean hasMediaProperty(String filePath, int propertyKey);

    private static native void writeStringProperty(String filePath, int propertyKey, String propertyValue);
    private static native void writeIntProperty(String filePath, int propertyKey, int propertyValue);

    private static native void init();
    private MediaPropertyUtils() {}
}