package mediaprops;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class used for reading/checking/writing windows media file metadata properties
 * Reference: https://docs.microsoft.com/en-us/windows/win32/medfound/metadata-properties-for-media-files
 * @author Degubi
 */
public final class MediaPropertyUtils {
    private static final int NULL_UINT_VALUE = -1;

    static {
        System.loadLibrary("MediaProps");
        init();
    }

    /**
     * Function for reading out the value of a property. This function throws an {@link IllegalArgumentException} when the given property is empty
     * @param <T> Property value type
     * @param file The file to work with. If the file doesn't exist an {@link NoSuchFileException} gets thrown wrapped inside an {@link UncheckedIOException}
     * @param property The property to read the value of
     * @return Returns value of the given property or throws an {@link IllegalArgumentException} if the given property is empty
     * If the operation fails because of a file system error (e.g the file is in use) a {@link FileSystemException} gets thrown wrapped inside an {@link UncheckedIOException}
     */
    public static<T> T readProperty(Path file, MediaProperty<T> property) throws IllegalArgumentException, UncheckedIOException {
        var pathStr = checkedPath(file);

        switch(property.type) {
            case MediaProperty.STRING_FIELD_TYPE:
                var strRes = readStringProperty(pathStr, property.propOrdinal);
                if(strRes == null) {
                    throw new IllegalArgumentException("Property '" + property.name + "' doesn't exist on file: '" + pathStr + "'");
                }

                return (T) strRes;
            case MediaProperty.UINT_FIELD_TYPE:
                var intRes = readIntProperty(pathStr, property.propOrdinal);
                if(intRes == NULL_UINT_VALUE) {
                    throw new IllegalArgumentException("Property '" + property.name + "' doesn't exist on file: '" + pathStr + "'");
                }

                return (T) (Integer) intRes;
            default: return null; // Can't happen
        }
    }

    /**
     * Function for reading out the value of a property. This function doesn't throw when the given property is empty
     * @param <T> Property value type
     * @param file The file to work with. If the file doesn't exist an {@link NoSuchFileException} gets thrown wrapped inside an {@link UncheckedIOException}
     * @param property The property to read the value of
     * @return Returns the value boxed in an Optional<T> or Optional.empty if the given property is empty
     * If the operation fails because of a file system error (e.g the file is in use) a {@link FileSystemException} gets thrown wrapped inside an {@link UncheckedIOException}
     */
    public static<T> Optional<T> readOptionalProperty(Path file, MediaProperty<T> property) throws UncheckedIOException {
        var pathStr = checkedPath(file);

        switch(property.type) {
            case MediaProperty.STRING_FIELD_TYPE:
                var strRes = readStringProperty(pathStr, property.propOrdinal);

                return strRes == null ? Optional.empty() : Optional.of((T) strRes);
            case MediaProperty.UINT_FIELD_TYPE:
                var intRes = readIntProperty(pathStr, property.propOrdinal);

                return intRes == NULL_UINT_VALUE ? Optional.empty() : Optional.of((T) (Integer) intRes);
            default: return null; // Can't happen
        }
    }

    /**
     * Function for checking if a given property is set on a file
     * @param file The file to work with. If the file doesn't exist an {@link NoSuchFileException} gets thrown wrapped inside an {@link UncheckedIOException}
     * @param property The property to check
     * @return Returns true if the given file has the passed in property set
     * If the operation fails because of a file system error (e.g the file is in use) a {@link FileSystemException} gets thrown wrapped inside an {@link UncheckedIOException}
     */
    public static boolean hasProperty(Path file, MediaProperty<?> property) throws UncheckedIOException {
        return hasMediaProperty(checkedPath(file), property.propOrdinal);
    }

    /**
     * Function for clearing out the value of a property
     * @param file The file to work with. If the file doesn't exist an {@link NoSuchFileException} gets thrown wrapped inside an {@link UncheckedIOException}
     * @param property The property to clear
     * If the operation fails because of a file system error (e.g the file is in use) a {@link FileSystemException} gets thrown wrapped inside an {@link UncheckedIOException}
     */
    public static void clearProperty(Path file, MediaProperty<?> property) throws UncheckedIOException {
        clearMediaProperty(checkedPath(file), property.propOrdinal);
    }

    /**
     * Function for writing a value to a property. When null is passed the given property is cleared
     * @param <T> Property value type
     * @param file The file to work with. If the file doesn't exist an {@link NoSuchFileException} gets thrown wrapped inside an {@link UncheckedIOException}
     * @param property The property to write the value of
     * @param value Value to write, pass null to clear the property.
     * If the property's type is 'int' only positive values are accepted, otherwise a {@link IllegalArgumentException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link FileSystemException} gets thrown wrapped inside an {@link UncheckedIOException}
     */
    public static<T> void writeProperty(Path file, MediaProperty<T> property, T value) throws IllegalArgumentException, UncheckedIOException {
        var absPath = checkedPath(file);

        if(value == null) {
            clearMediaProperty(absPath, property.propOrdinal);
        }else{
            switch(property.type) {
                case MediaProperty.STRING_FIELD_TYPE:
                    writeStringProperty(absPath, property.propOrdinal, (String) value);
                    break;
                case MediaProperty.UINT_FIELD_TYPE:
                    var intVal = (int) value;
                    if(intVal < 0) {
                        throw new IllegalArgumentException("Can't set property '" + property.name + "' to a negative value! Tried to passed in value: '" + intVal + "'");
                    }

                    writeIntProperty(absPath, property.propOrdinal, intVal);
                    break;
                default: // Can't happen
            }
        }
    }


    private static String checkedPath(Path file) throws UncheckedIOException {
        var absPath = file.toAbsolutePath();
        if(!Files.exists(absPath)) {
            throw new UncheckedIOException(new NoSuchFileException("File doesn't exist: '" + file + "'"));
        }

        return absPath.toString();
    }

    private static native String readStringProperty(String filePath, int propertyKey);
    private static native int readIntProperty(String filePath, int propertyKey);

    private static native boolean hasMediaProperty(String filePath, int propertyKey);
    private static native void clearMediaProperty(String filePath, int propertyKey);

    private static native void writeStringProperty(String filePath, int propertyKey, String propertyValue);
    private static native void writeIntProperty(String filePath, int propertyKey, int propertyValue);

    private static native void init();
    private MediaPropertyUtils() {}
}