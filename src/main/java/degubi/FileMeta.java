package degubi;

import java.nio.file.*;

public final class FileMeta {
    static {
        System.loadLibrary("FileMetaLib");
        init();
    }


    public static void updateProperty(Path file, FileProperty property, Object value) {
        switch(property.type) {
            case STRING_FIELD_TYPE: updateStringProperty(file.toAbsolutePath().toString(), property.propOrdinal, (String) value); break;
            default: throw new IllegalStateException("Idk what happened...");
        }
    }


    private static native void updateStringProperty(String filePath, int propertyKey, String propertyValue);
    private static native void init();
    private FileMeta() {}

    static final int STRING_FIELD_TYPE = 0;
}