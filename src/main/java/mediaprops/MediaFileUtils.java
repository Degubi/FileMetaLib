package mediaprops;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import mediaprops.exception.*;

/**
 * Utility class used for reading/checking/writing windows media file metadata properties<br>
 * Reference: https://docs.microsoft.com/en-us/windows/win32/medfound/metadata-properties-for-media-files
 * @apiNote All functions in this class throw a {@link NonMediaFileException} if a passed in file is not a media file<br>
 *          All functions in this class throw a {@link MediaFileIOException} if the call function fails because of a file system error (e.g the file is in use)<br>
 *          All functions in this class throw a {@link FileDoesNotExistException} if the passed in file doesn't exist
 * @author Degubi
 */
public final class MediaFileUtils {
    private static final int NULL_UINT_VALUE = -1;

    static {
        try(var dllInputStream = MediaFileUtils.class.getResourceAsStream("/MediaPropUtils.dll")) {
            var dllOutputPath = Path.of(System.getProperty("java.io.tmpdir") + "/MediaPropUtils.dll");

            Files.copy(dllInputStream, dllOutputPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(dllOutputPath.toAbsolutePath().toString());
            init();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to extract MediaPropUtils.dll!", e);
        }
    }

    /**
     * Function for reading out the value of a property
     * @param <T> Property value type
     * @param source The file to read the property from
     * @param property The property to read the value of
     * @return Returns the value wrapped in an Optional<T> or Optional.empty if the given property is empty
     */
    public static<T> Optional<T> readProperty(Path source, MediaProperty<T> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        var absSource = checkedPath(source);

        switch(property.type) {
            case MediaProperty.STRING_FIELD_TYPE:
                var strRes = readStringProperty(absSource, property.propOrdinal);

                return strRes == null ? Optional.empty() : Optional.of((T) strRes);
            case MediaProperty.UINT_FIELD_TYPE:
                var intRes = readIntProperty(absSource, property.propOrdinal);

                return intRes == NULL_UINT_VALUE ? Optional.empty() : Optional.of((T) (Integer) intRes);
            default: return null; // Can't happen
        }
    }

    /**
     * Function for checking if a given property exists on a file
     * @param source The file to work with
     * @param property The property to check
     * @return Returns true if the given file has the passed in property set
     */
    public static boolean hasProperty(Path source, MediaProperty<?> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        return hasMediaProperty(checkedPath(source), property.propOrdinal);
    }

    /**
     * Function for checking if a given file is a valid media file
     * @param source The file to work with
     * @return Returns true if the given file is a valid media file
     */
    public static boolean isMediaFile(Path source) throws FileDoesNotExistException {
        return isValidMediaFile(checkedPath(source));
    }

    /**
     * Function for copying the value of a property from a source to a target file
     * @param source The file to copy from
     * @param target The file to copy into
     * @param property The property to copy
     */
    public static void copyProperty(Path source, Path target, MediaProperty<?> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        writePropertyInternal(checkedPath(target), property, readProperty(source, property).orElse(null));
    }

    /**
     * Function for clearing out the value of a property
     * @param target The file to clear out the property from
     * @param property The property to clear
     */
    public static void clearProperty(Path target, MediaProperty<?> property) throws FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        clearMediaProperty(checkedPath(target), property.propOrdinal);
    }

    /**
     * Function for writing a property. When null is passed the given property is cleared
     * @param <T> Property value type
     * @param target The file to write the property into
     * @param property The property to write the value of
     * @param value The value to write, pass null to clear the property.
     * If the property's type is 'int' only positive values are accepted, otherwise a {@link IllegalArgumentException} gets thrown
     */
    public static<T> void writeProperty(Path target, MediaProperty<T> property, T value) throws IllegalArgumentException, FileDoesNotExistException, MediaFileIOException, NonMediaFileException {
        writePropertyInternal(checkedPath(target), property, value);
    }


    private static String checkedPath(Path file) throws FileDoesNotExistException {
        var absPath = file.toAbsolutePath();
        if(!Files.exists(absPath)) {
            throw new FileDoesNotExistException("File doesn't exist: '" + absPath + "'");
        }

        return absPath.toString();
    }

    private static void writePropertyInternal(String absTarget, MediaProperty<?> property, Object value) {
        if(value == null) {
            clearMediaProperty(absTarget, property.propOrdinal);
        }else{
            switch(property.type) {
                case MediaProperty.STRING_FIELD_TYPE:
                    writeStringProperty(absTarget, property.propOrdinal, (String) value);
                    break;
                case MediaProperty.UINT_FIELD_TYPE:
                    var intVal = (int) value;
                    if(intVal < 0) {
                        throw new IllegalArgumentException("Can't set property '" + property.name + "' to a negative value! Tried to passed in value: '" + intVal + "'");
                    }

                    writeIntProperty(absTarget, property.propOrdinal, intVal);
                    break;
                default: break; // Can't happen
            }
        }
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