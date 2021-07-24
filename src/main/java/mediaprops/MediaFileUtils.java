package mediaprops;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import mediaprops.exception.*;

/**
 * Utility class used for reading/checking/writing windows media file metadata properties
 * Reference: https://docs.microsoft.com/en-us/windows/win32/medfound/metadata-properties-for-media-files
 * @author Degubi
 */
public final class MediaFileUtils {
    private static final int NULL_UINT_VALUE = -1;

    static {
        try(var dllInputStream = MediaFileUtils.class.getResourceAsStream("/MediaProps.dll")) {
            var dllOutputPath = Path.of(System.getProperty("java.io.tmpdir") + "/MediaProps.dll");

            Files.copy(dllInputStream, dllOutputPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(dllOutputPath.toAbsolutePath().toString());
            init();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to extract MediaProps.dll!", e);
        }
    }

    /**
     * Function for reading out the value of a property. This function throws an {@link IllegalArgumentException} when the given property is empty
     * @param <T> Property value type
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @param property The property to read the value of
     * @return Returns value of the given property or throws an {@link IllegalArgumentException} if the given property is empty
     * If the given file is not a media file then a {@link NonMediaFileException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link MediaFileIOException} gets thrown
     */
    public static<T> T readProperty(Path file, MediaProperty<T> property) throws IllegalArgumentException, FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
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
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @param property The property to read the value of
     * @return Returns the value boxed in an Optional<T> or Optional.empty if the given property is empty
     * If the given file is not a media file then a {@link NonMediaFileException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link MediaFileIOException} gets thrown
     */
    public static<T> Optional<T> readOptionalProperty(Path file, MediaProperty<T> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
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
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @param property The property to check
     * @return Returns true if the given file has the passed in property set
     * If the given file is not a media file then a {@link NonMediaFileException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link MediaFileIOException} gets thrown
     */
    public static boolean hasProperty(Path file, MediaProperty<?> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        return hasMediaProperty(checkedPath(file), property.propOrdinal);
    }

    /**
     * Function for checking if a given file is a valid media file
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @return Returns true if the given file is a valid media file
     */
    public static boolean isMediaFile(Path file) throws FileDoesNotExistException {
        return isValidMediaFile(checkedPath(file));
    }

    /**
     * Function for clearing out the value of a property
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @param property The property to clear
     * If the given file is not a media file then a {@link NonMediaFileException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link MediaFileIOException} gets thrown
     */
    public static void clearProperty(Path file, MediaProperty<?> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        clearMediaProperty(checkedPath(file), property.propOrdinal);
    }

    /**
     * Function for writing a value to a property. When null is passed the given property is cleared
     * @param <T> Property value type
     * @param file The file to work with. If the file doesn't exist an {@link FileDoesNotExistException} gets thrown
     * @param property The property to write the value of
     * @param value Value to write, pass null to clear the property.
     * If the property's type is 'int' only positive values are accepted, otherwise a {@link IllegalArgumentException} gets thrown
     * If the given file is not a media file then a {@link NonMediaFileException} gets thrown
     * If the operation fails because of a file system error (e.g the file is in use) a {@link MediaFileIOException} gets thrown
     */
    public static<T> void writeProperty(Path file, MediaProperty<T> property, T value) throws IllegalArgumentException, FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
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


    private static String checkedPath(Path file) throws FileDoesNotExistException {
        var absPath = file.toAbsolutePath();
        if(!Files.exists(absPath)) {
            throw new FileDoesNotExistException("File doesn't exist: '" + file + "'");
        }

        return absPath.toString();
    }

    private static native String readStringProperty(String filePath, int propertyKey);
    private static native int readIntProperty(String filePath, int propertyKey);

    private static native boolean hasMediaProperty(String filePath, int propertyKey);
    private static native boolean isValidMediaFile(String filePath);
    private static native void clearMediaProperty(String filePath, int propertyKey);

    private static native void writeStringProperty(String filePath, int propertyKey, String propertyValue);
    private static native void writeIntProperty(String filePath, int propertyKey, int propertyValue);

    private static native void init();
    private MediaFileUtils() {}
}