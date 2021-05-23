package mediaprops;

/**
* Enum-like class for media file metadata properties
* Reference: https://docs.microsoft.com/en-us/windows/win32/medfound/metadata-properties-for-media-files
* @author Degubi
* @param <T> Property value type
*/
public final class MediaProperty<T> {
    static final int STRING_FIELD_TYPE = 0;
    static final int UINT_FIELD_TYPE = 1;

    public static final MediaProperty<String> AUTHOR = new MediaProperty<>(0, STRING_FIELD_TYPE, "Author");
    public static final MediaProperty<String> COMMENT = new MediaProperty<>(1, STRING_FIELD_TYPE, "Comment");
    public static final MediaProperty<String> COPYRIGHT = new MediaProperty<>(2, STRING_FIELD_TYPE, "Copyright");
    public static final MediaProperty<String> KEYWORDS = new MediaProperty<>(3, STRING_FIELD_TYPE, "Keywords");
    public static final MediaProperty<String> LANGUAGE = new MediaProperty<>(4, STRING_FIELD_TYPE, "Language");
    public static final MediaProperty<String> SUB_TITLE = new MediaProperty<>(5, STRING_FIELD_TYPE, "Subtitle");
    public static final MediaProperty<Integer> YEAR = new MediaProperty<>(6, UINT_FIELD_TYPE, "Year");
    public static final MediaProperty<String> TITLE = new MediaProperty<>(7, STRING_FIELD_TYPE, "Title");

    final int propOrdinal;
    final int type;
    final String name;

    private MediaProperty(int propOrdinal, int type, String name) {
        this.propOrdinal = propOrdinal;
        this.type = type;
        this.name = name;
    }
}