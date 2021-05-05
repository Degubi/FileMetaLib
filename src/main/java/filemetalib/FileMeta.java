package filemetalib;

import java.nio.file.*;

public final class FileMeta {
    static {
        System.loadLibrary("FileMetaLib");
        init();
    }


    public static<T> T readProperty(Path file, FileProperty<T> property) {
        var absPath = file.toAbsolutePath();

        if(Files.exists(absPath)) {
            var pathStr = absPath.toString();

            switch(property.type) {
                case FileProperty.STRING_FIELD_TYPE: return (T) readStringProperty(pathStr, property.propOrdinal);
                default: throw new IllegalStateException("I can't believe you've done this...");
            }
        }

        throw new IllegalArgumentException("File doesn't exist: " + file);
    }

    public static<T> void writeProperty(Path file, FileProperty<T> property, T value) {
        var absPath = file.toAbsolutePath();

        if(Files.exists(absPath)) {
            var pathStr = absPath.toString();

            switch(property.type) {
                case FileProperty.STRING_FIELD_TYPE: writeStringProperty(pathStr, property.propOrdinal, (String) value); break;
                default: throw new IllegalStateException("I can't believe you've done this...");
            }
        }else{
            throw new IllegalArgumentException("File doesn't exist: " + file);
        }
    }

    private static native String readStringProperty(String filePath, int propertyKey);
    private static native void writeStringProperty(String filePath, int propertyKey, String propertyValue);
    private static native void init();
    private FileMeta() {}
}